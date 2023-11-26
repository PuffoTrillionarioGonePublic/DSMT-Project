use std::borrow::Cow;
use async_channel::{unbounded, Receiver, Sender};
use base64::Engine as _;
use rusqlite::Connection;
use rusqlite::Statement;
use rusqlite::ToSql;
use std::collections::HashMap;
use std::fmt::Debug;
use std::fmt::Display;
use std::fmt::Formatter;
use std::fs::DirEntry;
use std::ops::Deref;
use std::ops::DerefMut;
use std::ops::FromResidual;
use std::path::{Path, PathBuf};
use std::rc::Rc;
use std::sync::Arc;
use std::time::Duration;
use tokio::task;
use tokio::task::LocalSet;
use uuid::Uuid;

#[derive(Clone)]
pub struct SQLiteValue(pub rusqlite::types::Value);

impl ToSql for SQLiteValue {
    fn to_sql(&self) -> rusqlite::Result<rusqlite::types::ToSqlOutput<'_>> {
        self.0.to_sql()
    }
}

impl Deref for SQLiteValue {
    type Target = rusqlite::types::Value;
    fn deref(&self) -> &Self::Target {
        &self.0
    }
}

impl DerefMut for SQLiteValue {
    fn deref_mut(&mut self) -> &mut Self::Target {
        &mut self.0
    }
}

impl Display for SQLiteValue {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        Debug::fmt(&self.0, f)
    }
}

impl Debug for SQLiteValue {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        Debug::fmt(&self.0, f)
    }
}

pub type Result<T> = std::result::Result<T, RusqliteError>;

pub enum RusqliteError {
    RusqliteError(rusqlite::Error),
    CommunicationError(String),
    IoError(std::io::Error),
    CustomError(String),
}

impl Display for RusqliteError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            RusqliteError::RusqliteError(e) => std::fmt::Debug::fmt(e, f)?,
            RusqliteError::CommunicationError(s) => std::fmt::Debug::fmt(s, f)?,
            RusqliteError::CustomError(s) => std::fmt::Debug::fmt(s, f)?,
            RusqliteError::IoError(e) => std::fmt::Debug::fmt(e, f)?,
        }
        Ok(())
    }
}

impl Debug for RusqliteError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            RusqliteError::RusqliteError(e) => std::fmt::Debug::fmt(e, f)?,
            RusqliteError::CommunicationError(s) => std::fmt::Debug::fmt(s, f)?,
            RusqliteError::CustomError(s) => std::fmt::Debug::fmt(s, f)?,
            RusqliteError::IoError(e) => std::fmt::Debug::fmt(e, f)?,
        }
        Ok(())
    }
}

impl From<rusqlite::Error> for RusqliteError {
    fn from(e: rusqlite::Error) -> Self {
        RusqliteError::RusqliteError(e)
    }
}

impl From<RusqliteError> for rusqlite::Error {
    fn from(e: RusqliteError) -> Self {
        match e {
            RusqliteError::RusqliteError(e) => e,
            _ => rusqlite::Error::SqliteFailure(
                rusqlite::ffi::Error {
                    code: rusqlite::ffi::ErrorCode::Unknown,
                    extended_code: 0xcafebab,
                },
                None,
            ),
        }
    }
}

impl<T> From<async_channel::SendError<T>> for RusqliteError {
    fn from(e: async_channel::SendError<T>) -> Self {
        let mut s = String::new();
        let mut fmt = Formatter::new(&mut s);
        std::fmt::Debug::fmt(&e, &mut fmt).unwrap();
        RusqliteError::CommunicationError(s)
    }
}

impl From<std::io::Error> for RusqliteError {
    fn from(e: std::io::Error) -> Self {
        RusqliteError::IoError(e)
    }
}

impl FromResidual<std::io::Error> for RusqliteError {
    fn from_residual(residual: std::io::Error) -> Self {
        RusqliteError::IoError(residual)
    }
}

impl<T> FromResidual<async_channel::SendError<T>> for RusqliteError {
    fn from_residual(residual: async_channel::SendError<T>) -> Self {
        let mut s = String::new();
        let mut fmt = Formatter::new(&mut s);
        std::fmt::Debug::fmt(&residual, &mut fmt).unwrap();
        RusqliteError::CommunicationError(s)
    }
}

impl From<async_channel::RecvError> for RusqliteError {
    fn from(e: async_channel::RecvError) -> Self {
        let mut s = String::new();
        let mut fmt = Formatter::new(&mut s);
        std::fmt::Debug::fmt(&e, &mut fmt).unwrap();
        RusqliteError::CommunicationError(s)
    }
}

impl FromResidual<async_channel::RecvError> for RusqliteError {
    fn from_residual(residual: async_channel::RecvError) -> Self {
        let mut s = String::new();
        let mut fmt = Formatter::new(&mut s);
        std::fmt::Debug::fmt(&residual, &mut fmt).unwrap();
        RusqliteError::CommunicationError(s)
    }
}

pub enum ContextInput {
    Create(Box<str>),
    Close,
}

pub enum ContextOutput {
    Create(Sender<ConnectionInput>, Receiver<ConnectionOutput>),
    Done,
    Error(RusqliteError),
}

pub enum ConnectionInput {
    Execute(Box<str>, Box<[SQLiteValue]>),
    Prepare(Box<str>),
    LastInsertRowId,
    Changes,
    BusyTimeout(Duration),
    Close,
}

pub enum ConnectionOutput {
    Prepare(Result<(Sender<StmtInput>, Receiver<StmtOutput>)>),
    Execute(Result<usize>),
    Changes(Result<u64>),
    BusyTimeout(Result<()>),
    LastInsertRowid(i64),
    Done,
    Error(RusqliteError),
}

#[derive(Debug)]
pub enum StmtInput {
    StepBy(usize),
    ClearBindings,
    CloneAndReset,
    Bind(usize, SQLiteValue),
    BindParameterCount,
    ColumnNames,
    ColumnName(usize),
    ColumnCount,
    Close,
}

pub enum StmtOutput {
    Rows(Result<Vec<Vec<SQLiteValue>>>),
    ClearBindings(Result<bool>),
    CloneAndReset(Result<(Sender<StmtInput>, Receiver<StmtOutput>)>),
    Bind(Result<()>),
    BindParameterCount(Result<usize>),
    ColumnNames(Result<Arc<[Box<str>]>>),
    ColumnName(Result<Box<str>>),
    ColumnCount(Result<usize>),
    Done,
    Error(RusqliteError),
}

#[derive(Debug)]
pub struct Context {
    home: PathBuf,
    sender: Sender<ContextInput>,
    receiver: Receiver<ContextOutput>,
    uuid: Uuid,
    join_handle: Option<std::thread::JoinHandle<Result<()>>>,
}

impl Drop for Context {
    fn drop(&mut self) {
        let _ = self.sender.send_blocking(ContextInput::Close);
        let join_handle = self.join_handle.take();
        let Some(join_handle) = join_handle else {
            return;
        };
        let _ = join_handle.join();
    }
}

#[derive(Debug)]
pub struct VirtualConnection {
    sender: Sender<ConnectionInput>,
    receiver: Receiver<ConnectionOutput>,
    // keep track of the uuid to the context to check consistency,
    uuid: Uuid,
    context: Uuid,
}

#[derive(Debug)]
pub struct VirtualStatement {
    sender: Sender<StmtInput>,
    receiver: Receiver<StmtOutput>,
    connection: Uuid,
    context: Uuid,
}

fn row_to_vec(ncols: usize, row: &rusqlite::Row) -> Vec<SQLiteValue> {
    let mut vec = Vec::with_capacity(ncols);
    for i in 0..ncols {
        let val = if let Ok(name) = row.get(i) {
            rusqlite::types::Value::Text(name)
        } else if let Ok(name) = row.get(i) {
            rusqlite::types::Value::Integer(name)
        } else if let Ok(name) = row.get(i) {
            rusqlite::types::Value::Real(name)
        } else if let Ok(name) = row.get(i) {
            rusqlite::types::Value::Blob(name)
        } else {
            rusqlite::types::Value::Null
        };
        let val = SQLiteValue(val);
        vec.push(val);
    }
    vec
}

async fn handle_bind_parameter_count(
    sender: &Sender<StmtOutput>,
    statement_meta: &StatementMeta,
) -> Result<()> {
    sender
        .send(StmtOutput::BindParameterCount(Ok(
            statement_meta.parameter_count
        )))
        .await?;
    Ok(())
}

async fn handle_column_count(
    sender: &Sender<StmtOutput>,
    statement_meta: &StatementMeta,
) -> Result<()> {
    sender
        .send(StmtOutput::ColumnCount(Ok(statement_meta
            .column_names
            .len())))
        .await?;
    Ok(())
}

async fn handle_clone_and_reset(
    sender: &Sender<StmtOutput>,
    statement_meta: &StatementMeta,
) -> Result<()> {
    let (stmt_sender, other_thread_receiver) = unbounded();
    let (other_thread_sender, stmt_receiver) = unbounded();
    let connection = Rc::clone(&statement_meta.connection);
    let query = Rc::clone(&statement_meta.query);
    let parameters_to_bind = Some(statement_meta.bound_values.clone());
    task::spawn_local(async move {
        statement(
            stmt_sender,
            stmt_receiver,
            connection,
            query,
            parameters_to_bind,
        )
        .await
    });
    sender
        .send(StmtOutput::CloneAndReset(Ok((
            other_thread_sender,
            other_thread_receiver,
        ))))
        .await?;
    Ok(())
}

async fn handle_column_name(
    sender: &Sender<StmtOutput>,
    index: usize,
    statement_meta: &StatementMeta,
) -> Result<()> {
    let col = statement_meta.column_names.get(index);

    if let Some(col) = col {
        sender
            .send(StmtOutput::ColumnName(Ok(col.to_owned())))
            .await?;
    } else {
        sender
            .send(StmtOutput::ColumnName(Ok(Default::default())))
            .await?;
    }
    Ok(())
}

async fn handle_column_names(
    sender: &Sender<StmtOutput>,
    statement_meta: &StatementMeta,
) -> Result<()> {
    sender
        .send(StmtOutput::ColumnNames(Ok(Arc::clone(
            &statement_meta.column_names,
        ))))
        .await?;
    Ok(())
}

async fn handle_bind(
    stmt: &mut Statement<'_>,
    statement_meta: &mut StatementMeta,
    sender: &Sender<StmtOutput>,
    n: usize,
    value: SQLiteValue,
) -> Result<()> {
    let value = Rc::new(value);
    stmt.raw_bind_parameter(n, Rc::clone(&value))?;
    statement_meta.bound_values.insert(n, value);
    sender.send(StmtOutput::Bind(Ok(()))).await?;
    Ok(())
}

// 0 is not allowed, so use it as special value to signal close
async fn handle_stmt_inside_step(
    receiver: &Receiver<StmtInput>,
    sender: &Sender<StmtOutput>,
    statement_meta: &mut StatementMeta,
) -> Result<usize> {
    loop {
        let input = receiver.recv().await?;
        match input {
            StmtInput::StepBy(n) => {
                if n == 0 {
                    return Err(RusqliteError::CustomError("Cannot step by 0".to_owned()));
                }
                return Ok(n);
            }
            StmtInput::ColumnName(index) => {
                handle_column_name(sender, index, statement_meta).await?
            }
            StmtInput::ColumnNames => handle_column_names(sender, statement_meta).await?,
            StmtInput::ColumnCount => handle_column_count(sender, statement_meta).await?,
            StmtInput::CloneAndReset => handle_clone_and_reset(sender, statement_meta).await?,
            StmtInput::ClearBindings => sender.send(StmtOutput::ClearBindings(Ok(false))).await?,
            StmtInput::BindParameterCount => {
                handle_bind_parameter_count(sender, statement_meta).await?
            }

            StmtInput::Bind(_, _) => {
                sender
                    .send(StmtOutput::Bind(Err(RusqliteError::CustomError(
                        "Cannot bind when stepping".to_owned(),
                    ))))
                    .await?
            }
            StmtInput::Close => {
                sender.send(StmtOutput::Done).await?;
                return Ok(0);
            }
        }
    }
}

async fn handle_step_by(
    stmt: &mut Statement<'_>,
    sender: &Sender<StmtOutput>,
    receiver: &Receiver<StmtInput>,
    mut n: usize,
    statement_meta: &mut StatementMeta,
) -> Result<bool> {
    let mut rows = stmt.raw_query();
    let mut tmp: Vec<Vec<SQLiteValue>> = Vec::new();
    let mut i = 0;
    let cols = rows
        .as_ref()
        .ok_or(RusqliteError::CustomError(
            "This shouldn't happen".to_owned(),
        ))?
        .column_names()
        .into_iter()
        .map(|s| s.to_owned())
        .collect::<Vec<_>>();

    if n == 0 {
        return Err(RusqliteError::CustomError("Cannot step by 0".to_owned()));
    }

    let mut first_step_done = false;

    loop {
        if i == n {
            if i == 0 {
                sender.send(StmtOutput::Done).await?;
                return Ok(false);
            }
            let tmp = std::mem::take(&mut tmp);
            sender.send(StmtOutput::Rows(Ok(tmp))).await?;
            n = handle_stmt_inside_step(receiver, &sender, statement_meta).await?;
            if n == 0 {
                return Ok(true);
            }
            i = 0;
        }

        {
            if let Ok(Some(row)) = rows.next() {
                let row = row_to_vec(cols.len(), row);
                tmp.push(row);
                i += 1;
            } else {
                if i == 0 {
                    sender.send(StmtOutput::Done).await?;
                    return Ok(false);
                }
                let tmp = std::mem::take(&mut tmp);
                sender.send(StmtOutput::Rows(Ok(tmp))).await?;
                n = handle_stmt_inside_step(receiver, &sender, statement_meta).await?;
                if n == 0 {
                    return Ok(true);
                }
                i = 0;
            };
        }
        if !first_step_done {
            let tmp = rows.as_ref().map(|x| {
                x.column_names()
                    .into_iter()
                    .map(ToString::to_string)
                    .map(String::into_boxed_str)
                    .collect::<Vec<_>>()
                    .into_boxed_slice()
                    .into()
            });
            if let Some(tmp) = tmp {
                statement_meta.column_names = tmp;
            }
            first_step_done = true;
        }
    }
}

struct StatementMeta {
    connection: Rc<Connection>,
    query: Rc<str>,
    column_names: Arc<[Box<str>]>,
    parameter_count: usize,
    bound_values: HashMap<usize, Rc<SQLiteValue>>,
}

async fn handle_statement(
    sender: &Sender<StmtOutput>,
    receiver: &Receiver<StmtInput>,
    conn: Rc<Connection>,
    query: Rc<str>,
    parameters_to_bind: Option<HashMap<usize, Rc<SQLiteValue>>>,
) -> Result<()> {
    let mut stmt = conn
        .prepare(&*query)
        .map_err(RusqliteError::RusqliteError)?;

    let column_names: Arc<[Box<str>]> = stmt
        .column_names()
        .into_iter()
        .map(ToString::to_string)
        .map(String::into_boxed_str)
        .collect::<Vec<_>>()
        .into_boxed_slice()
        .into();

    if let Some(ref parameters_to_bind) = parameters_to_bind {
        for (&n, value) in parameters_to_bind {
            stmt.raw_bind_parameter(n, value)?;
        }
    }

    let parameter_count = stmt.parameter_count();
    let mut statement_meta = StatementMeta {
        connection: Rc::clone(&conn),
        query,
        column_names,
        parameter_count,
        bound_values: parameters_to_bind.unwrap_or_default(),
    };

    loop {
        let input = receiver.recv().await?;
        match input {
            StmtInput::StepBy(n) => {
                if handle_step_by(&mut stmt, &sender, &receiver, n, &mut statement_meta).await? {
                    return Ok(());
                }
            }
            StmtInput::Bind(n, value) => {
                handle_bind(&mut stmt, &mut statement_meta, &sender, n, value).await?
            }
            StmtInput::BindParameterCount => {
                handle_bind_parameter_count(&sender, &statement_meta).await?
            }
            StmtInput::ClearBindings => {
                stmt.clear_bindings();
                sender.send(StmtOutput::ClearBindings(Ok(true))).await?
            }
            StmtInput::ColumnName(index) => {
                handle_column_name(&sender, index, &statement_meta).await?
            }
            StmtInput::CloneAndReset => handle_clone_and_reset(&sender, &statement_meta).await?,
            StmtInput::ColumnNames => handle_column_names(&sender, &statement_meta).await?,
            StmtInput::ColumnCount => handle_column_count(&sender, &statement_meta).await?,
            StmtInput::Close => {
                sender.send(StmtOutput::Done).await?;
                break;
            }
        }
    }
    Ok(())
}

async fn statement(
    sender: Sender<StmtOutput>,
    receiver: Receiver<StmtInput>,
    conn: Rc<Connection>,
    query: Rc<str>,
    parameters_to_bind: Option<HashMap<usize, Rc<SQLiteValue>>>,
) {
    if let Err(err) = handle_statement(&sender, &receiver, conn, query, parameters_to_bind).await {
        let _ = sender.send(StmtOutput::Error(err)).await;
    }
}

async fn handle_connection(
    conn_sender: &Sender<ConnectionOutput>,
    receiver: &Receiver<ConnectionInput>,
    connection: Rc<Connection>,
) -> Result<()> {
    loop {
        let op = receiver.recv().await?;
        match op {
            ConnectionInput::Prepare(query) => {
                let (stmt_sender, receiver) = unbounded();
                let (sender, stmt_receiver) = unbounded();
                let connection = Rc::clone(&connection);
                let query = Rc::from(query);
                task::spawn_local(async move {
                    statement(stmt_sender, stmt_receiver, connection, query, None).await
                });
                conn_sender
                    .send(ConnectionOutput::Prepare(Ok((sender, receiver))))
                    .await?;
            }

            ConnectionInput::Changes => {
                conn_sender
                    .send(ConnectionOutput::Changes(Ok(connection.changes())))
                    .await?;
            }

            ConnectionInput::Execute(query, params) => {
                let params = params.iter().map(|v| v as &dyn ToSql).collect::<Vec<_>>();
                let rv = connection.execute(&*query, params.as_slice());
                conn_sender
                    .send(ConnectionOutput::Execute(rv.map_err(RusqliteError::from)))
                    .await?;
            }

            ConnectionInput::LastInsertRowId => {
                conn_sender
                    .send(ConnectionOutput::LastInsertRowid(
                        connection.last_insert_rowid(),
                    ))
                    .await?
            }

            ConnectionInput::BusyTimeout(timeout) => {
                conn_sender
                    .send(ConnectionOutput::BusyTimeout(
                        connection
                            .busy_timeout(timeout)
                            .map_err(RusqliteError::from),
                    ))
                    .await?
            }

            ConnectionInput::Close => {
                conn_sender.send(ConnectionOutput::Done).await?;
                return Ok(());
            }
        }
    }
}

async fn connection(
    sender: Sender<ConnectionOutput>,
    receiver: Receiver<ConnectionInput>,
    connection: Rc<Connection>,
) {
    if let Err(err) = handle_connection(&sender, &receiver, connection).await {
        let _ = sender.send(ConnectionOutput::Error(err)).await;
    }
}

fn do_create_context(
    context_sender: Sender<ContextOutput>,
    context_receiver: Receiver<ContextInput>,
) -> Result<()> {
    let rt = tokio::runtime::Builder::new_current_thread()
        .enable_io()
        .enable_time()
        .build()
        .map_err(|_| RusqliteError::CustomError("Can't start runtime".to_owned()))?;
    let ls = LocalSet::new();
    rt.block_on(async move {
        ls.run_until(async {
            loop {
                let op = context_receiver.recv().await?;
                match op {
                    ContextInput::Create(file) => {
                        let conn = Connection::open(&*file)?;
                        let (conn_sender, receiver) = unbounded();
                        let (sender, conn_receiver) = unbounded();
                        task::spawn_local(async move {
                            connection(conn_sender, conn_receiver, Rc::new(conn)).await
                        });
                        context_sender.send_blocking(ContextOutput::Create(sender, receiver))?;
                    }
                    ContextInput::Close => {
                        context_sender.send_blocking(ContextOutput::Done)?;
                        return Ok(());
                    }
                }
            }
        })
        .await
    })
}

pub fn create_context(home: &str) -> Result<Context> {
    let (conn_th_sender, receiver) = unbounded();
    let (sender, conn_th_receiver) = unbounded();
    let uuid = Uuid::new_v4();
    let join_handle =
        std::thread::spawn(move || do_create_context(conn_th_sender, conn_th_receiver));
    let join_handle = Some(join_handle);
    let home = PathBuf::from(home);
    Ok(Context {
        home,
        sender,
        receiver,
        uuid,
        join_handle,
    })
}

fn check_connection_consistency(ctx: &Context, conn: &VirtualConnection) -> Result<()> {
    if conn.context != ctx.uuid {
        return Err(RusqliteError::CustomError(
            "Connection's context isn't consistent".to_owned(),
        ));
    }
    Ok(())
}

fn check_statement_consistency(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
) -> Result<()> {
    check_connection_consistency(ctx, conn)?;
    if conn.uuid != stmt.connection {
        return Err(RusqliteError::CustomError(
            "Statement's connection isn't consistent".to_owned(),
        ));
    }
    if ctx.uuid != stmt.context {
        return Err(RusqliteError::CustomError(
            "Statement's context isn't consistent".to_owned(),
        ));
    }
    Ok(())
}

fn mangle_bucket(bucket: &str) -> String {
    let bucket = base64::engine::general_purpose::URL_SAFE_NO_PAD.encode(bucket);
    bucket
}

fn mangle_filename(filename: &str) -> String {
    let filename = base64::engine::general_purpose::URL_SAFE_NO_PAD.encode(filename);
    format!("{}.db", filename)
}

fn bucket_target_path(home: &PathBuf, bucket: &str) -> PathBuf {
    let mangled_bucket = mangle_bucket(bucket);
    home.join(mangled_bucket)
}

fn file_target_path(home: &PathBuf, bucket: &str, filename: &str) -> PathBuf {
    let mangled_filename = mangle_filename(filename);
    bucket_target_path(home, bucket).join(mangled_filename)
}

pub fn create_connection(ctx: &Context, bucket: &str, filename: &str) -> Result<VirtualConnection> {
    let (sender, receiver) = (&ctx.sender, &ctx.receiver);
    let directory_target = bucket_target_path(&ctx.home, bucket);
    if !directory_target.exists() {
        std::fs::create_dir_all(&directory_target)?;
    }
    let file = file_target_path(&ctx.home, bucket, filename);
    let file = file
        .to_str()
        .ok_or(RusqliteError::CustomError(
            "Cannot convert path to str".to_owned(),
        ))?
        .to_string()
        .into_boxed_str();
    sender.send_blocking(ContextInput::Create(file))?;
    let ContextOutput::Create(sender, receiver) = receiver.recv_blocking()? else {
        unreachable!()
    };
    Ok(VirtualConnection {
        sender,
        receiver,
        uuid: Uuid::new_v4(),
        context: ctx.uuid,
    })
}


pub fn list_buckets(ctx: &Context) -> Result<Vec<String>> {
    let read_dir = std::fs::read_dir(&ctx.home)?;
    let mut buckets = Vec::new();
    for entry in read_dir {
        let Ok(entry) = entry else {
            continue;
        };
        let path = entry.path();
        if !path.is_dir() {
            continue;
        }
        get_filename(&path).map(|x| buckets.push(x));
    }
    Ok(buckets)
}

pub fn list_files(ctx: &Context, bucket: &str) -> Result<Vec<String>> {
    let directory_target = bucket_target_path(&ctx.home, bucket);
    let read_dir = std::fs::read_dir(directory_target)?;
    let mut files = Vec::new();
    for entry in read_dir {
        let Ok(entry) = entry else {
            continue;
        };
        let path = entry.path();
        if !path.is_file() {
            continue;
        }
        get_filename(&path).map(|x| files.push(x));
    }
    Ok(files)
}




fn get_filename(path: &PathBuf) -> Option<String> {
    let file_name = path
        .file_stem()?
        .to_str()?;
    let file_name = base64::engine::general_purpose::URL_SAFE_NO_PAD.decode(file_name);

    file_name
        .as_ref()
        .map(Vec::as_slice)
        .map(String::from_utf8_lossy)
        .as_ref()
        .map(Cow::to_string)
        .ok()
}

pub fn delete_file(ctx: &Context, bucket: &str, filename: &str) -> Result<()> {
    let file_target = file_target_path(&ctx.home, bucket, filename);
    std::fs::remove_file(file_target)?;
    Ok(())
}

pub fn delete_bucket(ctx: &Context, bucket: &str) -> Result<()> {
    let bucket_target = bucket_target_path(&ctx.home, bucket);
    std::fs::remove_dir_all(bucket_target)?;
    Ok(())
}

pub fn libversion(ctx: &Context) -> String {
    rusqlite::version().to_owned()
}

fn do_conn<T>(
    ctx: &Context,
    conn: &VirtualConnection,
    input: ConnectionInput,
    f: impl Fn(ConnectionOutput) -> Result<T>,
) -> Result<T> {
    check_connection_consistency(ctx, conn)?;
    let (sender, receiver) = (&conn.sender, &conn.receiver);
    // do not unwrap or return error, but wait for error received by receiver
    let _ = sender.send_blocking(input);
    let tmp = receiver.recv_blocking()?;
    if let ConnectionOutput::Error(err) = tmp {
        return Err(err);
    }
    f(tmp)
}

pub fn set_busy_timeout(ctx: &Context, conn: &VirtualConnection, timeout: Duration) -> Result<()> {
    do_conn(
        ctx,
        conn,
        ConnectionInput::BusyTimeout(timeout),
        |tmp| match tmp {
            ConnectionOutput::BusyTimeout(res) => res,
            _ => unreachable!(),
        },
    )
}

pub fn prepare(ctx: &Context, conn: &VirtualConnection, query: &str) -> Result<VirtualStatement> {
    do_conn(
        ctx,
        conn,
        ConnectionInput::Prepare(query.to_owned().into_boxed_str()),
        |tmp| match tmp {
            ConnectionOutput::Prepare(res) => {
                let (sender, receiver) = res?;
                Ok(VirtualStatement {
                    sender,
                    receiver,
                    connection: conn.uuid,
                    context: ctx.uuid,
                })
            }
            _ => unreachable!(),
        },
    )
}

pub fn execute(
    ctx: &Context,
    conn: &VirtualConnection,
    query: &str,
    params: Vec<SQLiteValue>,
) -> Result<usize> {
    do_conn(
        ctx,
        conn,
        ConnectionInput::Execute(
            query.to_string().into_boxed_str(),
            params.into_boxed_slice(),
        ),
        |tmp| match tmp {
            ConnectionOutput::Execute(rv) => rv,
            _ => unreachable!(),
        },
    )
}

pub fn close(ctx: &Context, conn: &VirtualConnection) -> Result<()> {
    do_conn(ctx, conn, ConnectionInput::Close, |tmp| match tmp {
        ConnectionOutput::Done => Ok(()),
        _ => unreachable!(),
    })
}

pub fn last_insert_rowid(ctx: &Context, conn: &VirtualConnection) -> Result<i64> {
    do_conn(
        ctx,
        conn,
        ConnectionInput::LastInsertRowId,
        |tmp| match tmp {
            ConnectionOutput::LastInsertRowid(id) => Ok(id),
            _ => unreachable!(),
        },
    )
}

pub fn changes(ctx: &Context, conn: &VirtualConnection) -> Result<u64> {
    do_conn(ctx, conn, ConnectionInput::Changes, |tmp| match tmp {
        ConnectionOutput::Changes(changes) => changes,
        _ => unreachable!(),
    })
}

fn do_stmt<T>(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
    input: StmtInput,
    f: impl Fn(StmtOutput) -> Result<T>,
) -> Result<T> {
    check_statement_consistency(ctx, conn, stmt)?;
    let (sender, receiver) = (&stmt.sender, &stmt.receiver);
    // do not unwrap or return error, but wait for error received by receiver
    let _ = sender.send_blocking(input);
    let tmp = receiver.recv_blocking()?;
    if let StmtOutput::Error(err) = tmp {
        return Err(err);
    }
    f(tmp)
}

pub fn bind(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
    n: usize,
    value: SQLiteValue,
) -> Result<()> {
    do_stmt(
        ctx,
        conn,
        stmt,
        StmtInput::Bind(n, value),
        |tmp| match tmp {
            StmtOutput::Bind(res) => res,
            _ => unreachable!(),
        },
    )
}

pub fn column_names(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
) -> Result<Arc<[Box<str>]>> {
    do_stmt(ctx, conn, stmt, StmtInput::ColumnNames, |tmp| match tmp {
        StmtOutput::ColumnNames(res) => res,
        _ => unreachable!(),
    })
}

pub fn column_count(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
) -> Result<usize> {
    do_stmt(ctx, conn, stmt, StmtInput::ColumnCount, |tmp| match tmp {
        StmtOutput::ColumnCount(res) => res,
        _ => unreachable!(),
    })
}

pub fn clear_bindings(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
) -> Result<bool> {
    do_stmt(ctx, conn, stmt, StmtInput::ClearBindings, |tmp| match tmp {
        StmtOutput::ClearBindings(res) => res,
        _ => unreachable!(),
    })
}

pub fn clone_and_reset(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
) -> Result<VirtualStatement> {
    do_stmt(ctx, conn, stmt, StmtInput::CloneAndReset, |tmp| match tmp {
        StmtOutput::CloneAndReset(res) => {
            let (sender, receiver) = res?;
            Ok(VirtualStatement {
                sender,
                receiver,
                connection: conn.uuid,
                context: ctx.uuid,
            })
        }
        _ => unreachable!(),
    })
}

pub fn bind_parameter_count(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
) -> Result<usize> {
    do_stmt(
        ctx,
        conn,
        stmt,
        StmtInput::BindParameterCount,
        |tmp| match tmp {
            StmtOutput::BindParameterCount(res) => res,
            _ => unreachable!(),
        },
    )
}

pub fn finalize(ctx: &Context, conn: &VirtualConnection, stmt: &VirtualStatement) -> Result<()> {
    do_stmt(ctx, conn, stmt, StmtInput::Close, |tmp| match tmp {
        StmtOutput::Done => Ok(()),
        _ => unreachable!(),
    })
}

pub fn step_by(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
    n: usize,
) -> Result<Option<Vec<Vec<SQLiteValue>>>> {
    do_stmt(ctx, conn, stmt, StmtInput::StepBy(n), |tmp| match tmp {
        StmtOutput::Done => Ok(None),
        StmtOutput::Rows(rows) => rows.map(|x| Some(x.into())),
        _ => unreachable!(),
    })
}

pub fn database_list(
    ctx: &Context,
    connection: &VirtualConnection,
) -> Result<Option<Vec<(usize, String, String)>>> {
    let stmt = prepare(ctx, connection, "PRAGMA database_list")?;
    Ok(step_all(ctx, connection, &stmt)?.map(|mut rows| {
        rows.iter_mut()
            .map(|row| {
                let rusqlite::types::Value::Integer(seq) = row[0].0 else {
                    panic!()
                };
                let rusqlite::types::Value::Text(ref mut db_name) = row[1].0 else {
                    panic!()
                };
                let db_name = std::mem::take(db_name);
                let rusqlite::types::Value::Text(ref mut path) = row[2].0 else {
                    panic!()
                };
                let path = std::mem::take(path);
                // convert path to PathBuf
                let path = std::path::PathBuf::from(path);
                let file_name = path.file_name().unwrap().to_str().unwrap();
                let file_name = &file_name[..file_name.len() - 3];
                let file_name = base64::engine::general_purpose::URL_SAFE_NO_PAD
                    .decode(file_name.as_bytes())
                    .unwrap();
                let file_name = String::from_utf8_lossy(&file_name).to_string();
                (seq as usize, db_name, file_name)
            })
            .collect::<Vec<_>>()
    }))
}

pub fn step_all(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
) -> Result<Option<Vec<Vec<SQLiteValue>>>> {
    let mut res = Vec::new();
    loop {
        match step_by(ctx, conn, stmt, 100)? {
            Some(tmp) => res.extend(tmp),
            None => break,
        }
    }
    if res.is_empty() {
        Ok(None)
    } else {
        Ok(Some(res))
    }
}

pub fn column_name(
    ctx: &Context,
    conn: &VirtualConnection,
    stmt: &VirtualStatement,
    n: usize,
) -> Result<Box<str>> {
    do_stmt(ctx, conn, stmt, StmtInput::ColumnName(n), |tmp| match tmp {
        StmtOutput::ColumnName(name) => name,
        _ => unreachable!(),
    })
}
