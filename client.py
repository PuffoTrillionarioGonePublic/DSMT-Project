from enum import Enum
import base64
import logging
import sys
import requests
from types import SimpleNamespace

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
logger.addHandler(logging.StreamHandler())

NODE0="http://localhost:8080" 
NODE1="http://localhost:8081" 
NODE2="http://localhost:8082"

NODE = NODE0

class ERLDBError(Exception):
	pass

class Client:
	def __init__(self):
		self.last_response = None

	def __getattr__(self, name):
		def wrapper(**kwargs):
			res = requests.post(f"{NODE}?target={name}", json=kwargs)
			logger.debug(f"{'Call':=^20}: {name}(**{kwargs})")
			logger.debug(f"{'Headers':=^20}: {res.headers}")
			logger.debug(f"{'Response':=^20}: {res.text}\n")
			self.last_response = res
			try:
				res.raise_for_status()
				return res.json()["ok"]
			except Exception as e:
				raise ERLDBError(f"Error: {res.text}") from e
		return wrapper
	
class Connection():
	def __init__(self, client: Client, *, Bucket: str, File: str):
		self.client = client
		self.file = File
		self.bucket = Bucket
		self.conn: int = None

	def __enter__(self):
		self.conn = self.client.create_connection(Bucket=self.bucket, File=self.file)
		return self

	def __exit__(self, *a):
		self.client.close(Conn=self.conn)
		self.conn = None
	
	def __getattr__(self, *a, **kw):
		if self.conn is None:
			raise Exception("Connection not open")
		func = self.client.__getattr__(*a, **kw)
		def wrapper(*args, **kw):
			return func(*args, **kw, Conn=self.conn)
		return wrapper

class SQLType(Enum):
	Null = 0
	Integer = 1
	Float = 2
	Text = 3
	Blob = 4

class SQLValue(list):
	def __init__(self, type: SQLType, value: any):
		super().__init__([type.value, value])

	@property
	def type(self):
		return SQLType(self[0])
	
	@property
	def value(self):
		return self[1]	

	def __repr__(self):
		if self.type == SQLType.Null:
			return "null"
		elif self.type == SQLType.Blob:
			return repr(base64.urlsafe_b64decode(self.value.encode() + b"==="))
		return repr(self.value)
	
	def __str__(self):
		if self.type == SQLType.Null:
			return "null"
		elif self.type == SQLType.Blob:
			return str(base64.urlsafe_b64decode(self.value.encode() + b"==="))
		return str(self.value)
	
	@classmethod
	def from_py(cls, value: any):
		if value is None:
			return cls(type=SQLType.Null, value=None)
		elif isinstance(value, int):
			return cls(type=SQLType.Integer, value=value)
		elif isinstance(value, float):
			return cls(type=SQLType.Float, value=value)
		elif isinstance(value, str):
			return cls(type=SQLType.Text, value=value)
		elif isinstance(value, bytes):
			return cls(type=SQLType.Blob, value=base64.urlsafe_b64encode(value).decode().rstrip("="))
		raise Exception(f"Unknown type: {type(value)}")
	
	@classmethod
	def from_erl(cls, tv: list):
		return cls(type=SQLType(tv[0]), value=tv[1])

	
class Statement():
	def __init__(self, connection: Connection, *, Query: str, Args: list = []):
		self.connection = connection
		self.query = Query
		self.args = Args
		self.stmt: int = None
	
	def __enter__(self):
		self.stmt = self.connection.prepare(Query=self.query)
		for i, arg in enumerate(self.args):
			self.connection.bind(Stmt=self.stmt, N=i+1, Value=SQLValue.from_py(arg))
		return self
	
	def __exit__(self, *a):
		self.connection.finalize(Stmt=self.stmt)
		self.stmt = None
	
	def __getattr__(self, *a, **kw):
		if self.stmt is None:
			raise Exception("Statement not open")
		func = self.connection.__getattr__(*a, **kw)
		def wrapper(*args, **kw):
			return func(*args, **kw, Stmt=self.stmt)
		return wrapper
	
	def __iter__(self):
		return self
	
	def __next__(self):
		row = self.step_by(N=1)
		if row is None:
			raise StopIteration
		return [SQLValue.from_erl(r) for r in row[0]]


def test_types():
	U("test.db")
	Q("select ?, ?, ?, ?, ?", [None, 1, 2.0, "3", b"4"])

def load_database():
    c = Client()
    with Connection(c, Bucket="private", File="credentials") as conn:
        table_creation = """
		CREATE TABLE IF NOT EXISTS credentials (   
			id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
			username TEXT NOT NULL UNIQUE,
			password TEXT NOT NULL,
			is_admin INT DEFAULT 0 NOT NULL,
			created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
      	);""".strip()

        conn.execute(Query=table_creation, Params=[])

        table_insertion = f"""
			INSERT INTO credentials (username, password, is_admin, created_at) VALUES (?, ?, ?, ?);
		""".strip()
        rows = [
            ['admin', '$2a$10$QQF8Viy/EjJfM3Ow0hT1/ON1lsVLnHXn.xdERGat52c3gMEnP9iSK', 1, '2023-01-01 02:16:52'],
            ['ciaone', '$2a$10$j36afHT0tfxYDe7jWgryRu1YQ.QTrJhM8uUCdu3V.bmrinWKQ9KkC', 0, '2023-01-01 03:50:15.344'],
            ['ciaone1', '$2a$10$W9kOKfVArhENiWJsdWBDp.CNjv.Y1Nr461sovWrtCNkTl7kosEZ/a', 0, '2023-01-01 03:59:14.546'],
            ['plutis', '$2a$10$rRvB4VpmsBbNuPrt.TOZEe51RmNL3vn4W3nuhZA3KP8cvWwhwbLiO', 0, '2023-01-01 01:15:55.42231'],
            ['user1', '$2a$10$65rABltqsjBzZgQhPfMAFudO0u6jCEWiXLs3ajn2Tuz9WF6akwJLy', 0, '2023-01-04 00:23:41.307'],
            ['user2', '$2a$10$oHMZFbFElZ/dmZywDPBgjuP22LRZNddNGTDaolIa3O./b8M0upV6y', 0, '2023-01-09 15:21:55.294'],
            ['user3', '$2a$10$nsHPPoYwPlPmAN1wi.i.COVLvKv/vG4Z32TzVbcnqjETJqS74ul7O', 0, '2023-01-09 16:51:30.568'],
            ['test1', '$2a$10$4pmqSMtaIE1J/qMfPmOBEuY3hvpPlA9VOwKI6yDvUboV.7FvQ7btm', 0, '2023-01-01 01:15:55.42231']
        ]

        for params in rows:
            params = [SQLValue.from_py(x) for x in params]
            conn.execute(Query=table_insertion, Params=params)

		# create a table containing only username and file
        table_creation = """
            CREATE TABLE IF NOT EXISTS privileges (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                filename TEXT NOT NULL,
                UNIQUE(username, filename),
                FOREIGN KEY (username) REFERENCES credentials(username) ON DELETE CASCADE
                
            );
            """.strip()
        # 

        conn.execute(Query=table_creation, Params=[])
        table_insertion = f"""
			INSERT INTO privileges (username, filename) VALUES (?, ?);
		""".strip()
        rows = [
            ['ciaone', 'public/magic'],
            ['plutis', 'public/magic'],
        ]

        for params in rows:
            params = [SQLValue.from_py(x) for x in params]
            conn.execute(Query=table_insertion, Params=params)


    with Connection(c, Bucket="public", File="magic") as conn:
        tables_and_dump = [
            ["""
            CREATE TABLE IF NOT EXISTS mythical_creatures (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                creature_name TEXT NOT NULL UNIQUE,
                habitat TEXT NOT NULL,
                danger_level INTEGER NOT NULL,
                last_sighted REAL -- could represent a timestamp as a Julian day number
            );
            """, """
            INSERT INTO mythical_creatures (creature_name, habitat, danger_level, last_sighted) VALUES
            ('Dragon', 'Volcanic mountains', 10, 2459581.5),
            ('Griffin', 'Highland prairies', 7, 2459582.5),
            ('Mermaid', 'Coral reefs', 3, 2459583.5);
            ('Unicorn', 'Enchanted forest', 5, 2459584.5);
            ('Phoenix', 'Volcanic mountains', 9, 2459585.5);
            ('Sphinx', 'Desert', 8, 2459586.5);
            ('Cyclops', 'Cave', 6, 2459587.5);
            ('Centaur', 'Highland prairies', 4, 2459588.5);
            ('Pegasus', 'Highland prairies', 2, 2459589.5);
            ('Minotaur', 'Cave', 1, 2459590.5);
            ('Basilisk', 'Desert', 0, 2459591.5);
            

            """],
           [ """
            CREATE TABLE IF NOT EXISTS enchanted_books (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL UNIQUE,
                author TEXT NOT NULL,
                magic_intensity INTEGER NOT NULL,
                price REAL NOT NULL -- price could be a floating point number
            );
            """, 
            """
            INSERT INTO enchanted_books (title, author, magic_intensity, price) VALUES
            ('Spells of the Ancients', 'Magus Eldritch', 9, 79.99),
            ('Potions and Brews', 'Alchemist Aurelius', 6, 49.99),
            ('The Enchanted Forest', 'Sage Sylvanus', 5, 39.99);
            """],
            ["""
            CREATE TABLE IF NOT EXISTS alchemy_ingredients (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                ingredient_name TEXT NOT NULL UNIQUE,
                rarity_level INTEGER NOT NULL,
                potency REAL NOT NULL -- potency could be a decimal representing strength
            );
            """, 
            """INSERT INTO alchemy_ingredients (ingredient_name, rarity_level, potency) VALUES
                ('Phoenix Feather', 9, 0.95),
                ('Unicorn Horn', 8, 0.89),
                ('Dragon Scale', 7, 0.78);
            """]
        ]
        
        for table_creation, table_insertion in tables_and_dump:
            conn.execute(Query=table_creation, Params=[])
            conn.execute(Query=table_insertion, Params=[])

if __name__ == "__main__":
	if sys.argv[1] == "load":
		load_database()
		exit(0)
	print(f"Either pass the 'load' argument to load the database or run interactively")

# to use in interactive mode
glob = SimpleNamespace()

def U(database, bucket="default"):
	if hasattr(glob, "conn"):
		glob.conn.__exit__()
	c = Client()
	conn = Connection(c, Bucket=bucket, File=database)
	conn.__enter__()
	glob.conn = conn
	return "ok"

def Q(query, args=[]):
	with Statement(glob.conn, Query=query, Args=args) as stmt:
		head: list[str] = stmt.column_names()
		rows: list[list[any]] = [*stmt]
		maxs = [max(len(str(c)) for c in col) for col in zip(*[head, *rows])]
		if head:
			for line in [head, ['='*m for m in maxs], *rows]:
				print(" | ".join(str(c).ljust(m) for c, m in zip(line, maxs)))
			print()
	changes = glob.conn.changes()
	print("Lines changed:", changes)

def X():
	glob.conn.__exit__()
	exit(0)
