%% Copyright (c) 2018 Pivotal Software Inc, All Rights Reserved.
%%
%% Licensed under the Apache License, Version 2.0 (the "License");
%% you may not use this file except in compliance with the License.
%% You may obtain a copy of the License at
%%
%%       https://www.apache.org/licenses/LICENSE-2.0
%%
%% Unless required by applicable law or agreed to in writing, software
%% distributed under the License is distributed on an "AS IS" BASIS,
%% WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
%% See the License for the specific language governing permissions and
%% limitations under the License.
%%

-module(erlang_project_ra).

-behaviour(ra_machine).

-include_lib("erlang_project.hrl").

-export(
  [
    % ra_machine
    init/1,
    apply/3,
    % user api
    single_node_call/3,
    cluster_call/3
  ]
).

init(_Config) ->
  % create a unique data dir for each run because in case of a crash we will restart from zero
  {ok, DataDir} = application:get_env(erlang_project, sql_data_dir),
  {Mega, Sec, Micro} = erlang:timestamp(),
  Timestamp = integer_to_list(Mega * 1000000000000 + Sec * 1000000 + Micro),
  UniqueDataDir = filename:join([DataDir, Timestamp]),
  filelib:ensure_dir(UniqueDataDir),
  {ok, Ctx} = my_nif:create_context(list_to_binary(UniqueDataDir)),
  #app_state{index = 0, term = 0, ctx = Ctx, conns = #{}, stmts = #{}}.


-spec single_node_call(Leader :: ra:server_id(), TargetFunc :: api_fun(), Args :: api_args()) ->
  {ok, {api_ret(), ra:index(), ra:term()}, ra:server_id()} | {error, term()} | {timeout, term()}.
single_node_call(Leader, TargetFunc, Args) ->
  case
  ra:consistent_query(
    Leader,
    fun
      (#app_state{index = Index, term = Term} = State) -> {TargetFunc(State, Args), Index, Term}
    end
  ) of
    {ok, {{_State, Result}, Index, Term}, Leader1} -> {ok, {Result, Index, Term}, Leader1};
    Err -> Err
  end.


-spec cluster_call(Leader :: ra:server_id(), TargetFunc :: api_fun(), Args :: api_args()) ->
  {ok, {api_ret(), ra:index(), ra:term()}, ra:server_id()} | {error, term()} | {timeout, term()}.
cluster_call(Leader, TargetFunc, Args) -> ra:process_command(Leader, {api_call, TargetFunc, Args}).

% called by raft on every node when a new log entry is committed
apply(#{index := Index, term := Term} = _Metadata, {api_call, TargetFunc, Args} = _Command, State) ->
  State1 = State#app_state{index = Index, term = Term},
  {State2, Result} = TargetFunc(State1, Args),
  % never do side effects for now
  SideEffects = side_effects(Index, State2),
  % return also Index, Term for debugging purposes
  {State2, {Result, Index, Term}, SideEffects}.


% We take a snapshot every `release_cursor_every` log entries.
% (release cursor side effect means taking a snapshot)
side_effects(RaftIndex, MachineState) ->
  case application:get_env(erlang_project, release_cursor_every) of
    undefined -> [];
    {ok, NegativeOrZero} when NegativeOrZero =< 0 -> [];

    {ok, Every} ->
      case RaftIndex rem Every of
        0 -> [{release_cursor, RaftIndex, MachineState}];
        _ -> []
      end
  end.
