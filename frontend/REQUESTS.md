# Requests

errorMsg
```
{
    "error" : "message"
}
```

---

`POST /admin/register/`

👑 Authorization : `Bearer token`

req:

```
{
    "username" : "string",
    "email" : "string",
    "password" : "string"
}
```

resp:

```
{
    "message" : "string"
}
```

--- 

`DELETE /admin/user/{id}`

👑 Authorization : `Bearer token`

req:

```
```

resp:

```
{
    "message" : "string"
}
```
---

`POST /user/signin/`

req:

```
{
    "username" : "string",
    "password" : "string"
}
```

resp:
```
{
    "username" : "username",
    "expirationDate" : "date",
    "token" : "token",
    "admin" : false
}
```

---

`POST /logged/set_busy_timeout/`

req:

Authorization : `Bearer token`

```
{
    "ms": 100,
}
```

resp:
```
{
    "ms": 100,
}
```

---

`GET /logged/lib_version/`

req:
```
```

resp:
```
"v1.2.0"
```

---


`POST /admin/db/`

👑 Authorization : `Bearer token`

req:
```
{
    "name" : "string"
}
```

resp:

```
```

---

`DELETE /admin/db/{name}`

👑 Authorization : `Bearer token`

req:
```
```

resp:
```
```

---

`POST /admin/grant`

👑 Authorization : `Bearer token`

req:
```
{
    "usernames": ["user1", "user2", "user3"],
    "tables": ["table1", "table2"]
}

```

resp:
```
```

---


`GET /logged/databases/`

Authorization : `Bearer token`

req:
```
```

resp:
```
{
    "databases": [
        "string",
        "string",
        "string",
        "string",
    ]
}
```

---

`GET /logged/tables

Authorization : `Bearer token`

req:
```

{
    "db": "dbname"
}

```

resp:
```
{
    "tables": [
        "table1",
        "table2",
        "table3"
    ]
}
```

---

`POST /logged/columns`

Authorization : `Bearer token`

req:
```
```

resp:
```
{
    "name":"string",
    "columns" : [
        "name": "string",
        "type": "string"
    ]
}
```

---


`POST /logged/query/`


Authorization : `Bearer token`

req:
```
{
    "db" : "string",
    "query" : "string"
}
```

resp:
```
{
    "columnNames": ["pippo", "pluto"],
    "rows" : [
        [[3, "lorem ipsum"], [1, 21]],
        [[3, "dolor sit amet"], [1, 2141]]
    ]
}
```

---

`POST /logged/statement/`


Authorization : `Bearer token`

req:
```
{
    "db" : "string",
    "query" : "query", 
    "params" [
        [0, "null"],
        [1, 3],
        [2, 3.14],
        [3, "just a string without c null terminator"],
        [4, "VGhlIHF1aWNrIGJyb3duIGZveCBqdW1wcyBvdmVyIHRoZSBsYXp5IGRvZw=="]
    ]
}
```

resp:
```
{
    "columnNames": ["pippo", "pluto"],
    "rows" : [
        [[3, "lorem ipsum"], [1, 21]],
        [[3, "dolor sit amet"], [1, 2111]]
    ]
}
```
