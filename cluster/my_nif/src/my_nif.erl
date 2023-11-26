-module(my_nif).

-export([
    %start/2,
    create_context/1,
    set_busy_timeout/3,
    create_connection/3,
    prepare/3,
    bind/5,
    column_names/3,
    column_count/3,
    execute/4,
    bind_parameter_count/3,
    clear_bindings/3,
    finalize/3,
    close/2,
    last_insert_rowid/2,
    changes/2,
    list_buckets/1,
    step_by/4,
    clone_and_reset/3,
    lib_version/1,
    column_name/4,
    generate_uuid/0,
    list_files/2,
    delete_file/3,
    delete_bucket/2,
    main/0
]).

-include("cargo.hrl").
-on_load(init/0).
-define(NOT_LOADED, not_loaded(?LINE)).

%%%===================================================================
%%% API
%%%===================================================================

set_busy_timeout(_Ctx, _Conn, _Timeout) -> ?NOT_LOADED.

create_context(_Home) -> ?NOT_LOADED.

create_connection(_Ctx, _Bucket, _File) -> ?NOT_LOADED.

prepare(_Ctx, _Conn, _Query) -> ?NOT_LOADED.

bind(_Ctx, _Conn, _Stmt, _N, _Value) -> ?NOT_LOADED.

column_names(_Ctx, _Conn, _Stmt) -> ?NOT_LOADED.

column_count(_Ctx, _Conn, _Stmt) -> ?NOT_LOADED.

execute(_Ctx, _Conn, _Query, _Params) -> ?NOT_LOADED.

bind_parameter_count(_Ctx, _Conn, _Stmt) -> ?NOT_LOADED.

clear_bindings(_Ctx, _Conn, _Stmt) -> ?NOT_LOADED.

finalize(_Ctx, _Conn, _Stmt) -> ?NOT_LOADED.

close(_Ctx, _Conn) -> ?NOT_LOADED.

last_insert_rowid(_Ctx, _Conn) -> ?NOT_LOADED.

changes(_Ctx, _Conn) -> ?NOT_LOADED.

step_by(_Ctx, _Conn, _Stmt, _N) -> ?NOT_LOADED.

column_name(_Ctx, _Conn, _Stmt, _N) -> ?NOT_LOADED.

generate_uuid() -> ?NOT_LOADED.

clone_and_reset(_Ctx, _Conn, _Stmt) -> ?NOT_LOADED.

list_files(_Ctx, _Bucket) -> ?NOT_LOADED.

list_buckets(_Ctx) -> ?NOT_LOADED.

lib_version(_Ctx) -> ?NOT_LOADED.

delete_file(_Ctx, _Bucket, _File) -> ?NOT_LOADED.

delete_bucket(_Ctx, _Bucket) -> ?NOT_LOADED.

%%%===================================================================
%%% NIF
%%%===================================================================

init() ->
    ?load_nif_from_crate(my_nif, 0).

not_loaded(Line) ->
    erlang:nif_error({not_loaded, [{module, ?MODULE}, {line, Line}]}).

%%%===================================================================
%%% Tests
%%%===================================================================
%
%-ifdef(TEST).
%-include_lib("eunit/include/eunit.hrl").
%
%add_test() ->
%    ?assertEqual(4, add(2, 2)).
%
%my_map_test() ->
%    ?assertEqual(#{lhs => 33, rhs => 21}, my_map()).
%
%my_maps_test() ->
%    ?assertEqual([#{lhs => 33, rhs => 21}, #{lhs => 33, rhs => 21}], my_maps()).
%
%my_tuple_test() ->
%    ?assertEqual({33, 21}, my_tuple()).
%
%unit_enum_echo_test() ->
%    ?assertEqual(foo_bar, unit_enum_echo(foo_bar)),
%    ?assertEqual(baz, unit_enum_echo(baz)).
%
%tagged_enum_echo_test() ->
%    ?assertEqual(foo, tagged_enum_echo(foo)),
%    ?assertEqual({bar, <<"string">>}, tagged_enum_echo({bar, <<"string">>})),
%    ?assertEqual({baz,#{a => 1, b => 2}}, tagged_enum_echo({baz,#{a => 1, b => 2}})).
%
%untagged_enum_echo_test() ->
%    ?assertEqual(123, untagged_enum_echo(123)),
%    ?assertEqual(<<"string">>, untagged_enum_echo(<<"string">>)).
%
%-endif.
%




main() ->
    {ok, Ctx} = create_context(<<"/tmp">>),
    {ok, Conn} = create_connection(Ctx, <<"db">>, <<"test.db">>),
    % create a table
    {ok, Count} = execute(Ctx, Conn, <<"CREATE TABLE IF NOT EXISTS foo (id INTEGER PRIMARY KEY, name TEXT NOT NULL, age INTEGER NOT NULL)">>, []),
    io:format("create table: ~p~n", [Count]),
    % insert some data
    {ok, Stmt} = prepare(Ctx, Conn, <<"INSERT INTO foo (name, age) VALUES ('Alice', 20)">>),
    %bind(Ctx, Conn, Stmt, 1, {<<"Alice">>),
    %bind(Ctx, Conn, Stmt, 2, 20),
    {ok, Res} = step_by(Ctx, Conn, Stmt, 1),
    io:format("insert: ~p~n", [Res]),
    finalize(Ctx, Conn, Stmt),
    % select some data
    {ok, Stmt1} = prepare(Ctx, Conn, <<"SELECT * FROM foo">>),
    {ok, Res1} = step_by(Ctx, Conn, Stmt1, 1),
    io:format("select: ~p~n", [Res1]),
    finalize(Ctx, Conn, Stmt1).

    

