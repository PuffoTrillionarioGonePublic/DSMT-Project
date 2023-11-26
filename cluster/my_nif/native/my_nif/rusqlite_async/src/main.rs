#![feature(try_trait_v2)]
#![feature(fmt_internals)]

mod connection;
use std::{collections::BTreeSet, time::Duration};

use connection::*;

use crate::connection::clone_and_reset;

fn get_result_set(
    ctx: &Context,
    conn: &VirtualConnection,
    statement: &VirtualStatement,
    result: Vec<Vec<SQLiteValue>>,
) -> Result<Vec<Vec<SQLiteValue>>> {
    let val = step_by(ctx, conn, statement, 100)?;
    match val {
        Some(mut val) => {
            let mut result = result;
            result.append(&mut val);
            get_result_set(ctx, conn, statement, result)
        }
        None => Ok(result),
    }
}

fn routine() -> Result<()> {
    println!(
        "thread {:?} waiting for connection",
        std::thread::current().id()
    );
    let name = format!("thread"); //, std::thread::current().id());
    let ctx = create_context("folder")?;
    let conn = create_connection(&ctx, "amico", &name)?;
    set_busy_timeout(&ctx, &conn, Duration::from_millis(500))?;
    println!("thread {:?} waiting execute 1", std::thread::current().id());
    execute(
        &ctx,
        &conn,
        "CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT)",
        Vec::new(),
    )?;
    println!("thread {:?} waiting execute 2", std::thread::current().id());
    execute(
        &ctx,
        &conn,
        "INSERT INTO test (name) VALUES ('foo')",
        Vec::new(),
    )?;
    println!("thread {:?} waiting execute 3", std::thread::current().id());
    execute(
        &ctx,
        &conn,
        "INSERT INTO test (name) VALUES ('bar')",
        Vec::new(),
    )?;
    println!("thread {:?} waiting execute 4", std::thread::current().id());
    execute(
        &ctx,
        &conn,
        "INSERT INTO test (name) VALUES ('baz')",
        Vec::new(),
    )?;
    println!("thread {:?} waiting prepare", std::thread::current().id());
    let stmt = prepare(&ctx, &conn, "SELECT * FROM test")?;
    println!(
        "thread {:?} waiting column_names",
        std::thread::current().id()
    );
    let cols = column_names(&ctx, &conn, &stmt)?;
    println!(
        "thread {:?} waiting result_set",
        std::thread::current().id()
    );

    let result_set = get_result_set(&ctx, &conn, &stmt, vec![])?;
    println!("result_set: {:?}", result_set);
    Ok(())
}

fn main2() {
    let mut v = Vec::new();
    for _ in 0..10 {
        v.push(std::thread::spawn(|| {
            for _ in 0..5 {
                let mut i = 0;
                while let Err(err) = routine() {
                    println!("thread {:?} error: {:?}", std::thread::current().id(), err);
                    eprintln!("retry {}", i);
                    i += 1;
                }
            }
        }));
    }

    for t in v {
        t.join().unwrap();
    }
}

fn opcode_writes(opcode: &str) -> bool {
    match opcode {
        // Opcodes that might write to the file
        "Insert" | "Update" | "Delete" | "NewRowid" | "WriteCookie" | "Commit" | "Rollback"
        | "AutoCommit" | "Savepoint" | "Release" | "JournalMode" | "Vacuum" | "Expire"
        | "InsertInt" | "InsertReal" | "RealAffinity" => true,
        _ => false,
    }
}

fn is_read_only(ctx: &Context, conn: &VirtualConnection, sql: &str) -> Result<bool> {
    let sql = format!("EXPLAIN {}", sql);
    let stmt = prepare(ctx, conn, &sql)?;
    let cols = column_names(ctx, conn, &stmt)?;

    // find index in cols where name is "opcode"
    let opcode_index = cols
        .iter()
        .enumerate()
        .find(|(_, col)| col.as_ref() == "opcode")
        .map(|(i, _)| i)
        .unwrap();

    let val = step_by(ctx, conn, &stmt, 100).unwrap().unwrap();
    let a: BTreeSet<String> = val
        .iter()
        .map(|row| {
            if let rusqlite::types::Value::Text(ref opcode) = row[opcode_index].0 {
                opcode.clone()
            } else {
                panic!("not a string")
            }
        })
        .collect();
    println!("a: {:?}", a);
    Ok(!val
        .iter()
        .map(|row| {
            if let rusqlite::types::Value::Text(ref opcode) = row[opcode_index].0 {
                opcode_writes(&opcode)
            } else {
                panic!("not a string")
            }
        })
        .any(|x| x))
}

fn main3() {
    let ctx = create_context("folder").unwrap();
    let conn = create_connection(&ctx, "db", "ciao2").unwrap();
    /*	conn = create_connection(filename)
    print(conn)
    exec = execute(conn['connectionId'], "CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT, value INTEGER)")
    print(exec)
    exec = execute(conn['connectionId'], "INSERT INTO test (name, value) VALUES ('foo', 1)")
    print(exec)
    exec = execute(conn['connectionId'], "INSERT INTO test (name, value) VALUES ('bar', 2)")
    print(exec)
    statement_id = prepare(conn['connectionId'], "SELECT * FROM test WGERE value > ?")
    print(statement_id)
    ret = bind(conn['connectionId'], statement_id['statementId'], 1, Value.Integer(1))
    print(ret)
    step = step_by(conn['connectionId'], statement_id['statementId'], 2)
    print(step) */

    let exec = execute(
        &ctx,
        &conn,
        "CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT, value INTEGER)",
        vec![],
    )
    .unwrap();
    println!("exec: {:?}", exec);
    let exec = execute(
        &ctx,
        &conn,
        "INSERT INTO test (name, value) VALUES ('foo', 1)",
        vec![],
    )
    .unwrap();
    println!("exec: {:?}", exec);
    let exec = execute(
        &ctx,
        &conn,
        "INSERT INTO test (name, value) VALUES ('bar', 2)",
        vec![],
    )
    .unwrap();
    println!("exec: {:?}", exec);
    //let statement_id = prepare(&ctx, &conn, "SELECT * FROM test WHERE value > ?").unwrap();
    let statement_id = prepare(
        &ctx,
        &conn,
        "INSERT INTO test(name, value) VALUES('ciao', 32)",
    )
    .unwrap();
    //let statement_id = prepare(&ctx, &conn, "SELECT * FROM test").unwrap();
    //let column_count = column_count(&ctx, &conn, &statement_id).unwrap_or(0);
    //println!("column_count: {:?}", column_count);
    // println!("statement_id: {:?}", statement_id);

    let step = step_by(&ctx, &conn, &statement_id, 1).unwrap();
    println!("step: {:?}", step);

    /*  let test_statements = vec![
        "CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, age INTEGER)",
        "INSERT INTO test (age) VALUES (25)",
        "UPDATE test SET age = 30 WHERE age = 25",
        "DELETE FROM test WHERE age = 30",
        "SELECT * FROM test",
        "BEGIN TRANSACTION",
        "COMMIT",
        "DROP TABLE test",
        "PRAGMA table_info(test)",
    ];

    // Now, iterate over each statement, print it, see if it's read-only, and execute it if necessary
    for sql in &test_statements {
        if let Ok(tmp) = is_read_only(&ctx, &conn, sql) {
            println!("SQL: {} is read-only: {}", sql, tmp);
            if tmp {
                println!("SQL: {} is read-only: {}", sql, "Executing");
                let prep = prepare(&ctx, &conn, sql).unwrap();
                let rv = step_by(&ctx, &conn, &prep, 100).unwrap();
                println!("rv: {:?}", rv);
            } else {
                println!("executing... {}", sql);
                if let Err(err) = execute(&ctx, &conn, sql, vec![]) {
                    println!("SQL: {} is read-only: {}", sql, "Error");
                    println!("Error: {:?}", err);
                }
            }
        } else {
            println!("SQL: {} is read-only: {}", sql, "Error");
            execute(&ctx, &conn, sql, vec![]).unwrap();
        }
    }*/
}

fn main() -> Result<()> {
    /* prep = prepare(
           conn["connectionId"],
           "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT NOT NULL, date TEXT DEFAULT CURRENT_TIMESTAMP)",
       )
       print(prep)
       ch = changes(conn["connectionId"])
       print(ch)
       count = column_count(conn["connectionId"], prep["statementId"])
       print(count)
       prep = prepare(conn["connectionId"], "INSERT INTO users(name) VALUES('ciao')")
       print(prep)
       ch = changes(conn["connectionId"])
       print(ch)
       count = column_count(conn["connectionId"], prep["statementId"])
       print(count)

    let ctx = create_context()?;
    let conn = create_connection(&ctx, "ciao")?;
    let prep = prepare(
        &ctx,
        &conn,
        "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT NOT NULL, date TEXT DEFAULT CURRENT_TIMESTAMP)",
    )?;
    println!("prep: {:?}", prep);
    step_by(&ctx, &conn, &prep, 100)?;
    let ch = changes(&ctx, &conn)?;
    println!("ch: {:?}", ch);
    let count = column_count(&ctx, &conn, &prep)?;
    println!("count: {:?}", count);
    for _ in 0..10 {
        let prep = prepare(&ctx, &conn, "INSERT INTO users(name) VALUES('ciao')")?;
        step_by(&ctx, &conn, &prep, 100)?;
    }
    println!("prep: {:?}", prep);
    let ch = changes(&ctx, &conn)?;
    println!("ch: {:?}", ch);
    let ch = column_names(&ctx, &conn, &prep)?;

    println!("ch: {:?}", ch);
    //let count = column_count(&ctx, &conn, &prep)?;
    //println!("count: {:?}", count);

    // iter over users
    let prep1 = prepare(&ctx, &conn, "SELECT * FROM users")?;
    println!("prep1: {:?}", step_by(&ctx, &conn, &prep1, 2)?);

    let ch = changes(&ctx, &conn)?;
    println!("ch: {:?}", ch);

    let connection = create_connection(&ctx, "ciao2")?;
    execute(
        &ctx,
        &connection,
        "CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT, value INTEGER)",
        vec![],
    )?;
    execute(
        &ctx,
        &connection,
        "INSERT INTO test (name, value) VALUES ('foo', 1)",
        vec![],
    )?;
    for _ in 0..10 {
        execute(
            &ctx,
            &connection,
            "INSERT INTO test (name, value) VALUES ('bar', 2)",
            vec![],
        )?;
    }

    let statement_id = prepare(
        &ctx,
        &connection,
        "SELECT * FROM test WHERE value > ?",
    )?;

    bind(&ctx, &connection, &statement_id, 1, SQLiteValue(rusqlite::types::Value::Integer(1)))?;
    let step = step_by(&ctx, &connection, &statement_id, 2)?;
    println!("step: {:?}", step);

    while let Some(x) = step_by(&ctx, &conn, &prep1, 20)? {
        println!("x: {:?}", x);
    }*/

    let ctx = create_context("folder")?;
    let conn = create_connection(&ctx, "amico", "2")?;
    execute(
        &ctx,
        &conn,
        "CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT, value INTEGER)",
        vec![],
    )?;

    execute(
        &ctx,
        &conn,
        "INSERT INTO test (name, value) VALUES ('foo', 1)",
        vec![],
    )?;

    execute(
        &ctx,
        &conn,
        "INSERT INTO test (name, value) VALUES ('bar', 2)",
        vec![],
    )?;

    execute(
        &ctx,
        &conn,
        "INSERT INTO test (name, value) VALUES ('baz', 3)",
        vec![],
    )?;

    let statement_id = prepare(&ctx, &conn, "SELECT * FROM test WHERE value > ?")?;
    bind(
        &ctx,
        &conn,
        &statement_id,
        1,
        SQLiteValue(rusqlite::types::Value::Integer(1)),
    )?;
    let step = step_by(&ctx, &conn, &statement_id, 1)?;
    println!("1) step: {:?}", step);

    let stmt2 = clone_and_reset(&ctx, &conn, &statement_id)?;
    let step = step_by(&ctx, &conn, &stmt2, 1)?;
    println!("2) step: {:?}", step);

    let step = step_by(&ctx, &conn, &statement_id, 1)?;
    println!("1) step: {:?}", step);

    let db_list = database_list(&ctx, &conn)?;
    println!("db_list: {:?}", db_list);
    // list files
    let file_list = list_files(&ctx, "amico")?;
    println!("file_list: {:?}", file_list);
    Ok(())
}
