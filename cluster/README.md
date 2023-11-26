erlang_project
=====

run with:
```sh
docker-compose up
```

interact with:
```python
$ python3 -i interact.py
>>> qr("select 1")
'[[1]]'
>>> # use qw when writeing something
>>> qw("CREATE TABLE my_table (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT)")
'[]'
>>> qw("INSERT INTO my_table (name) VALUES ('Alice'), ('Bob')")
'[]'
>>> # use qr when reading
>>> qr("SELECT * FROM my_table")
'[[1,"Alice"],[2,"Bob"]]'
>>> # you can also query another node in the cluster
>>> qr("SELECT * FROM my_table", NODE1)
```


# DEMO

attach new node to cluster:
```sh
docker run --rm -it --network=erlang_project_default -p 127.0.0.1:8083:8080 -e "LEADER=ra@node0.dsmt" --hostname="node3.dsmt" dsmt
```

connect to erlang shell of running node:
```sh
docker compose exec node0 erl -name hidden@node0.dsmt -hidden -setcookie secret
```

and then:
```erlang
> net_kernel:connect_node('ra@node0.dsmt').
^G
--> r 'ra@node0.dsmt'.
--> j
--> c

> {ok, Members, Leader} = ra:members({dsmt, node()}).
> ra:remove_member(Leader, {dsmt, 'ra@node3.dsmt'}).
```