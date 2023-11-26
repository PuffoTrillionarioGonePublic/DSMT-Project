from dataclasses import dataclass
from enum import Enum
import base64
import logging
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

# def main():
# 	c = Client(NODE0)
# 	with Connection(c, File="test.db") as conn:
# 		conn.execute(Query="CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT)", Params=[])
# 		conn.execute(Query="INSERT INTO test (name) VALUES (?)", Params=[SQLValue.from_py("foo")])
# 		conn.execute(Query="INSERT INTO test (name) VALUES (?)", Params=[SQLValue.from_py("bar")])

# 		with Statement(conn, Query="SELECT * FROM test") as stmt:
# 			print(stmt.column_names())
# 			for row in stmt:
# 				print(row)

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

# U("testdb")
# Q("INSERT INTO testx (name) VALUES (\"cuigi\"), (\"puligi\")")

def test_types():
	U("test.db")
	Q("select ?, ?, ?, ?, ?", [None, 1, 2.0, "3", b"4"])