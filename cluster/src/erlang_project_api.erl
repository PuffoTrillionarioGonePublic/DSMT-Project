-module(erlang_project_api).

-include_lib("erlang_project.hrl").

-export(
  [
    %
    % api for cowboy handler
    get_target_func/1,
    %
    % wrap my_nif functions
    list_buckets/2,
    lib_version/2,
    list_files/2,
    create_connection/2,
    set_busy_timeout/2,
    prepare/2,
    bind/2,
    column_names/2,
    column_count/2,
    execute/2,
    bind_parameter_count/2,
    clear_bindings/2,
    finalize/2,
    close/2,
    last_insert_rowid/2,
    changes/2,
    step_by/2,
    reset/2,
    column_name/2,
    delete_file/2,
    delete_bucket/2
  ]
).

-spec get_target_func(Target :: binary()) ->
  {ok, api_fun(), ClusterWideOp :: boolean()} | {error, Reason :: atom()}.
get_target_func(Target) ->
  ApiFun =
    case Target of
      <<"lib_version">> -> {ok, fun lib_version/2, false};
      <<"list_files">> -> {ok, fun list_files/2, false};
      <<"list_buckets">> -> {ok, fun list_buckets/2, false};
      <<"create_connection">> -> {ok, fun create_connection/2, true};
      <<"set_busy_timeout">> -> {ok, fun set_busy_timeout/2, true};
      <<"prepare">> -> {ok, fun prepare/2, true};
      <<"bind">> -> {ok, fun bind/2, true};
      <<"column_names">> -> {ok, fun column_names/2, false};
      <<"column_count">> -> {ok, fun column_count/2, false};
      <<"execute">> -> {ok, fun execute/2, true};
      <<"bind_parameter_count">> -> {ok, fun bind_parameter_count/2, false};
      <<"clear_bindings">> -> {ok, fun clear_bindings/2, true};
      <<"finalize">> -> {ok, fun finalize/2, true};
      <<"close">> -> {ok, fun close/2, true};
      <<"last_insert_rowid">> -> {ok, fun last_insert_rowid/2, false};
      <<"changes">> -> {ok, fun changes/2, false};
      <<"step_by">> -> {ok, fun step_by/2, true};
      <<"reset">> -> {ok, fun reset/2, true};
      <<"column_name">> -> {ok, fun column_name/2, false};
      <<"delete_file">> -> {ok, fun delete_file/2, true};
      <<"delete_bucket">> -> {ok, fun delete_bucket/2, true};
      _ -> {error, unknown_target}
    end,
  %
  % intercept functions and check that ConnId and StmtId are valid
  % and that the result is jsx serializable
  case ApiFun of
    {ok, Fun, ClusterWideOp} ->
      WrappedFun =
        fun
          (State, Args) ->
            logger:debug("Calling: ~p(~p)~n", [Target, Args]),
            try
              %
              % check that ConnId and StmtId are valid, (if in args)
              ok =
                case maps:find(<<"Conn">>, Args) of
                  {ok, ConnId} ->
                    case maps:find(ConnId, State#app_state.conns) of
                      {ok, _} -> ok;
                      _ -> throw(invalid_conn_id)
                    end;

                  _ -> ok
                end,
              ok =
                case maps:find(<<"Stmt">>, Args) of
                  {ok, StmtId} ->
                    case maps:find(StmtId, State#app_state.stmts) of
                      {ok, _} -> ok;
                      _ -> throw(invalid_stmt_id)
                    end;

                  _ -> ok
                end,
              %
              % call the function and make sure the result is jsx serializable
              {State1, Res} = Fun(State, Args),
              JsxRes =
                case Res of
                  {ok, {}} -> {ok, null};
                  {ok, R} -> {ok, R};
                  {error, Reason} -> {error, Reason};

                  Unexpected ->
                    logger:error("Unexpected: ~p~n", [Unexpected]),
                    {error, unexpected_result}
                end,
              _WillReturnThisIfNoThrow = {State1, [JsxRes]}
            catch
              throw : Why -> _WillReturnThisIfThrow = {State, [{error, Why}]}
            end
        end,
      {ok, WrappedFun, ClusterWideOp};

    Err -> Err
  end.


% {ok, version :: binary()}
lib_version(#app_state{ctx = Ctx} = State, #{} = _Args) ->
  Version = my_nif:lib_version(Ctx),
  {State, {ok, Version}};

lib_version(S, _) -> {S, {error, invalid_args}}.


% {ok, connid :: integer()}
create_connection(
  #app_state{index = Index, ctx = Ctx, conns = Conns} = State,
  #{<<"Bucket">> := Bucket, <<"File">> := File} = _Args
) ->
  case my_nif:create_connection(Ctx, Bucket, File) of
    {ok, Conn} ->
      ConnId = Index,
      {State#app_state{conns = maps:put(ConnId, Conn, Conns)}, {ok, ConnId}};

    Err -> {State, Err}
  end;

create_connection(S, _) -> {S, {error, invalid_args}}.


% {ok, [binary()]}
list_files(#app_state{ctx = Ctx} = State, #{<<"Bucket">> := Bucket} = _Args) ->
  {State, my_nif:list_files(Ctx, Bucket)};

list_files(S, _) -> {S, {error, invalid_args}}.

list_buckets(#app_state{ctx = Ctx} = State, #{} = _Args) -> {State, my_nif:list_buckets(Ctx)};
list_buckets(S, _) -> {S, {error, invalid_args}}.

% {ok , {}}
set_busy_timeout(
  #app_state{ctx = Ctx, conns = Conns} = State,
  #{<<"Conn">> := ConnId, <<"Timeout">> := Timeout} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  {State, my_nif:set_busy_timeout(Ctx, Conn, Timeout)};

set_busy_timeout(S, _) -> {S, {error, invalid_args}}.


% {ok, stmtid :: integer()}
prepare(
  #app_state{index = Index, ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Query">> := Query} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  StmtId = Index,
  case my_nif:prepare(Ctx, Conn, Query) of
    {ok, Stmt} -> {State#app_state{stmts = maps:put(StmtId, Stmt, Stmts)}, {ok, StmtId}};
    Err -> {State, Err}
  end;

prepare(S, _) -> {S, {error, invalid_args}}.


% {ok, {}}
bind(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId, <<"N">> := N, <<"Value">> := Value} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  % convert to sql format (i.e. [Type :: integer(), Value :: term()])
  ValueSql =
    case Value of
      [0, null] -> {0, null};
      [1, Int] when is_integer(Int) -> {1, Int};
      [2, Num] when is_number(Num) -> {2, float(Num)};
      [3, Str] when is_binary(Str) -> {3, Str};
      [4, Blob] when is_binary(Blob) -> {4, Blob};
      _ -> error
    end,
  case ValueSql of
    error -> {State, {error, invalid_value}};
    V -> {State, my_nif:bind(Ctx, Conn, Stmt, N, V)}
  end;

bind(S, _) -> {S, {error, invalid_args}}.


% {ok, [binary()]}
column_names(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  {State, my_nif:column_names(Ctx, Conn, Stmt)};

column_names(S, _) -> {S, {error, invalid_args}}.


% {ok, integer()}
column_count(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  {State, my_nif:column_count(Ctx, Conn, Stmt)};

column_count(S, _) -> {S, {error, invalid_args}}.


% {ok, integer()}
execute(
  #app_state{ctx = Ctx, conns = Conns} = State,
  #{<<"Conn">> := ConnId, <<"Query">> := Query, <<"Params">> := Params} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  ParamsSql = [{T, V} || [T, V] <- Params],
  {State, my_nif:execute(Ctx, Conn, Query, ParamsSql)};

execute(S, _) -> {S, {error, invalid_args}}.


% {ok, integer()}
bind_parameter_count(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId}
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  {State, my_nif:bind_parameter_count(Ctx, Conn, Stmt)};

bind_parameter_count(S, _) -> {S, {error, invalid_args}}.


% {ok, boolean()}
clear_bindings(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId}
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  {State, my_nif:clear_bindings(Ctx, Conn, Stmt)};

clear_bindings(S, _) -> {S, {error, invalid_args}}.


% {ok, {}}
finalize(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId}
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  % remove stmt from stmts
  _Res = my_nif:finalize(Ctx, Conn, Stmt),
  Stmts1 = maps:remove(StmtId, Stmts),
  State1 = State#app_state{stmts = Stmts1},
  {State1, {ok, {}}};

finalize(S, _) -> {S, {error, invalid_args}}.


% {ok, {}}
close(#app_state{ctx = Ctx, conns = Conns} = State, #{<<"Conn">> := ConnId} = _Args) ->
  Conn = maps:get(ConnId, Conns),
  % remove conn from conns
  _Res = my_nif:close(Ctx, Conn),
  Conns1 = maps:remove(ConnId, Conns),
  State1 = State#app_state{conns = Conns1},
  {State1, {ok, {}}};

close(S, _) -> {S, {error, invalid_args}}.


% {ok, integer()}
last_insert_rowid(#app_state{ctx = Ctx, conns = Conns} = State, #{<<"Conn">> := ConnId} = _Args) ->
  Conn = maps:get(ConnId, Conns),
  {State, my_nif:last_insert_rowid(Ctx, Conn)};

last_insert_rowid(S, _) -> {S, {error, invalid_args}}.


% {ok, integer()}
changes(#app_state{ctx = Ctx, conns = Conns} = State, #{<<"Conn">> := ConnId} = _Args) ->
  Conn = maps:get(ConnId, Conns),
  {State, my_nif:changes(Ctx, Conn)};

changes(S, _) -> {S, {error, invalid_args}}.


% {ok, [ Rows :: [ Vals :: [Type :: integer(), Value :: term()] ] ]
step_by(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId, <<"N">> := N} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  Res = my_nif:step_by(Ctx, Conn, Stmt, N),
  % convert to jsx serializable format
  JsxRes =
    case Res of
      {ok, nil} -> {ok, null};
      {ok, R} -> {ok, [[[T, V] || {T, V} <- Row] || Row <- R]};
      Err -> Err
    end,
  {State, JsxRes};

step_by(S, _) -> {S, {error, invalid_args}}.


% {ok, binary()}
column_name(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId, <<"N">> := N} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  {State, my_nif:column_name(Ctx, Conn, Stmt, N)};

column_name(S, _) -> {S, {error, invalid_args}}.


% {ok, {}}
reset(
  #app_state{ctx = Ctx, conns = Conns, stmts = Stmts} = State,
  #{<<"Conn">> := ConnId, <<"Stmt">> := StmtId} = _Args
) ->
  Conn = maps:get(ConnId, Conns),
  Stmt = maps:get(StmtId, Stmts),
  case my_nif:clone_and_reset(Ctx, Conn, Stmt) of
    {ok, NewStmt} ->
      my_nif:finalize(Ctx, Conn, Stmt),
      {State#app_state{stmts = maps:put(StmtId, NewStmt, Stmts)}, {ok, {}}};

    Err -> {State, Err}
  end;

reset(S, _) -> {S, {error, invalid_args}}.


% {ok, {}}
delete_file(#app_state{ctx = Ctx} = State, #{<<"Bucket">> := Bucket, <<"File">> := File} = _Args) ->
  {State, my_nif:delete_file(Ctx, Bucket, File)};

delete_file(S, _) -> {S, {error, invalid_args}}.

% {ok, {}}
delete_bucket(#app_state{ctx = Ctx} = State, #{<<"Bucket">> := Bucket} = _Args) ->
  {State, my_nif:delete_bucket(Ctx, Bucket)};

delete_bucket(S, _) -> {S, {error, invalid_args}}.
