# Build and run

the first time do (because the cluster is 3 times the same image and if it is not in cache it will build it 3 times)
```sh
for dir in $(ls -d */); do (cd $dir && echo building $dir && docker build .); done
```

then `docker compose up --build`, the app will be at http://localhost:8000

## populate the database

```sh
python3 client.py load
```

# Demo

`python3 -i client.py` and then
```py
U(database="credentials", bucket="private")
Q("SELECT * FROM credentials")
NODE
# kill node0
Q("SELECT * FROM credentials")
NODE = NODE1
Q("SELECT * FROM credentials")
glob.conn.client.last_response.headers

stmt = Statement(glob.conn, Query="SELECT * FROM credential")
stmt.__enter__()
next(stmt)
stmt.__exit__()
stmt = Statement(glob.conn, Query="SELECT * FROM credentials")
stmt.__enter__()
next(stmt)
NODE
NODE = NODE0
next(stmt)
[*stmt]
stmt.__exit__()
X()
```
