#![feature(try_trait_v2)]

use std::time::Duration;

use base64::Engine as _;
use rusqlite_async::connection::Result;
use rustler::Env;
use rustler::Term;

pub struct Context(rusqlite_async::connection::Context);
pub struct Connection(rusqlite_async::connection::VirtualConnection);
pub struct Statement(rusqlite_async::connection::VirtualStatement);

#[rustler::nif]
pub fn set_busy_timeout(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    timeout: u64,
) -> Result<()> {
    let timeout = Duration::from_millis(timeout);
    rusqlite_async::connection::set_busy_timeout(&ctx.0, &conn.0, timeout)
}

#[rustler::nif]
pub fn create_context(env: Env, home: String) -> Result<rustler::ResourceArc<Context>> {
    let ctx = rusqlite_async::connection::create_context(&home)?;
    let ctx = Context(ctx);
    Ok(rustler::ResourceArc::new(ctx))
}

#[rustler::nif]
pub fn create_connection(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    directory: String,
    file: String,
) -> Result<rustler::ResourceArc<Connection>> {
    let conn = rusqlite_async::connection::create_connection(&ctx.0, &directory, &file)?;
    Ok(rustler::ResourceArc::new(Connection(conn)))
}

#[rustler::nif]
pub fn prepare(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    query: String,
) -> Result<rustler::ResourceArc<Statement>> {
    let stmt = rusqlite_async::connection::prepare(&ctx.0, &conn.0, &query)?;
    Ok(rustler::ResourceArc::new(Statement(stmt)))
}


#[rustler::nif]
pub fn list_files(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    bucket: String,
) -> Result<Vec<String>> {
    rusqlite_async::connection::list_files(&ctx.0, &bucket)
}

#[rustler::nif]
pub fn list_buckets(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
) -> Result<Vec<String>> {
    rusqlite_async::connection::list_buckets(&ctx.0)
}


#[rustler::nif]
pub fn bind(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
    n: usize,
    value: rusqlite_async::connection::SQLiteValue,
) -> Result<()> {
    rusqlite_async::connection::bind(&ctx.0, &conn.0, &stmt.0, n, value)
}

#[rustler::nif]
pub fn column_names(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
) -> Result<Vec<String>> {
    rusqlite_async::connection::column_names(&ctx.0, &conn.0, &stmt.0)
        .map(|v| v.into_iter().map(ToString::to_string).collect())
}

#[rustler::nif]
pub fn column_count(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
) -> Result<usize> {
    rusqlite_async::connection::column_count(&ctx.0, &conn.0, &stmt.0)
}

#[rustler::nif]
pub fn execute(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    query: String,
    params: Vec<rusqlite_async::connection::SQLiteValue>,
) -> Result<usize> {
    rusqlite_async::connection::execute(&ctx.0, &conn.0, &query, params)
}

#[rustler::nif]
pub fn bind_parameter_count(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
) -> Result<usize> {
    rusqlite_async::connection::bind_parameter_count(&ctx.0, &conn.0, &stmt.0)
}

#[rustler::nif]
pub fn clone_and_reset(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
) -> Result<rustler::ResourceArc<Statement>> {
    let stmt = rusqlite_async::connection::clone_and_reset(&ctx.0, &conn.0, &stmt.0)?;
    Ok(rustler::ResourceArc::new(Statement(stmt)))
}

#[rustler::nif]
pub fn lib_version(env: Env, ctx: rustler::ResourceArc<Context>) -> String {
    rusqlite_async::connection::libversion(&ctx.0).to_string()
}

#[rustler::nif]
pub fn clear_bindings(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
) -> Result<bool> {
    rusqlite_async::connection::clear_bindings(&ctx.0, &conn.0, &stmt.0)
}

#[rustler::nif]
pub fn finalize(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
) -> Result<()> {
    rusqlite_async::connection::finalize(&ctx.0, &conn.0, &stmt.0)
}

#[rustler::nif]
pub fn close(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
) -> Result<()> {
    rusqlite_async::connection::close(&ctx.0, &conn.0)
}

#[rustler::nif]
pub fn last_insert_rowid(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
) -> Result<i64> {
    rusqlite_async::connection::last_insert_rowid(&ctx.0, &conn.0)
}

#[rustler::nif]
pub fn changes(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
) -> Result<u64> {
    rusqlite_async::connection::changes(&ctx.0, &conn.0)
}

#[rustler::nif]
pub fn step_by(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
    n: usize,
) -> Result<Option<Vec<Vec<rusqlite_async::connection::SQLiteValue>>>> {
    rusqlite_async::connection::step_by(&ctx.0, &conn.0, &stmt.0, n)
}

#[rustler::nif]
pub fn column_name(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    conn: rustler::ResourceArc<Connection>,
    stmt: rustler::ResourceArc<Statement>,
    n: usize,
) -> Result<String> {
    rusqlite_async::connection::column_name(&ctx.0, &conn.0, &stmt.0, n).map(String::from)
}

#[rustler::nif]
pub fn generate_uuid() -> String {
    let u = uuid::Uuid::new_v4();
    let mut bytes = u.as_bytes().to_vec();
    bytes.reverse();
    base64::engine::general_purpose::URL_SAFE_NO_PAD.encode(&bytes)
}



#[rustler::nif]
pub fn delete_file(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    bucket: String,
    file: String,
) -> Result<()> {
    rusqlite_async::connection::delete_file(&ctx.0, &bucket, &file)
}

#[rustler::nif]
pub fn delete_bucket(
    env: Env,
    ctx: rustler::ResourceArc<Context>,
    bucket: String,
) -> Result<()> {
    rusqlite_async::connection::delete_bucket(&ctx.0, &bucket)
}

fn load(env: Env, _info: Term) -> bool {
    rustler::resource!(Context, env);
    rustler::resource!(Connection, env);
    rustler::resource!(Statement, env);
    true
}

rustler::init!(
    "my_nif", // This should match the Erlang module you want to bind to
    [
        create_context,
        create_connection,
        prepare,
        bind,
        column_names,
        column_count,
        list_files,
        list_buckets,
        execute,
        lib_version,
        bind_parameter_count,
        clear_bindings,
        clone_and_reset,
        finalize,
        close,
        last_insert_rowid,
        changes,
        step_by,
        column_name,
        set_busy_timeout,
        generate_uuid,
        delete_file,
        delete_bucket,

    ],
    load = load
);
