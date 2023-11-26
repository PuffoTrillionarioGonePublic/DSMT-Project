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

-module(erlang_project_cow).

-behavior(cowboy_handler).

-export([init/2]).

init(Req = #{method := <<"POST">>, path := _Path}, State) ->
  Leader = proplists:get_value(leader, State),
  {ok, Body, _} = cowboy_req:read_body(Req),
  Args = jsx:decode(Body, [return_maps]),
  ReqQueryParams = cowboy_req:parse_qs(Req),
  Target = proplists:get_value(<<"target">>, ReqQueryParams, undefined),
  logger:debug("Target: ~p, Args: ~p~n", [Target, Args]),
  Result =
    case erlang_project_api:get_target_func(Target) of
      {error, unknown_target} -> {client_error, <<"unknown target">>};

      {ok, TargetFunc, _ClusterWideOp = false} ->
        erlang_project_ra:single_node_call(Leader, TargetFunc, Args);

      {ok, TargetFunc, _ClusterWideOp = true} ->
        erlang_project_ra:cluster_call(Leader, TargetFunc, Args)
    end,
  Resp =
    case Result of
      % in case of network partition, we will get a timeout from ra
      {timeout, Info} ->
        logger:error("Timeout: ~p~n", [Info]),
        cowboy_req:reply(503, #{}, "{\"error\":\"RA timeout\"}", Req);

      % should not happen
      {error, Info} ->
        logger:error("Error: ~p~n", [Info]),
        cowboy_req:reply(503, #{}, "{\"error\":\"RA error\"}", Req);

      % triggered only by unknown target
      {client_error, Info} ->
        logger:error("Client error: ~p~n", [Info]),
        cowboy_req:reply(400, #{}, jsx:encode([{error, Info}]), Req);

      % correct output from ra
      {ok, {Output, RaIndex, RaTerm}, Leader1} ->
        Headers =
          #{
            "content-type" => <<"application/json">>,
            "ra_index" => erl_to_str(RaIndex),
            "ra_leader" => erl_to_str(Leader1),
            "ra_term" => erl_to_str(RaTerm)
          },
        logger:info("Output: ~p~n", [Output]),
        Status =
          case Output of
            % ok response from rust nif
            [{ok, _}] -> 200;
            % error response from rust nif, probably a query error
            [{error, _}] -> 400;
            _ShouldNotHappen -> 500
          end,
        cowboy_req:reply(Status, Headers, jsx:encode(Output), Req)
    end,
  % update State with leader if needed
  MaybeNewLeader =
    case Result of
      {ok, _, L} -> L;
      _ -> Leader
    end,
  NewState = lists:map(fun ({leader, _}) -> {leader, MaybeNewLeader}; (Pair) -> Pair end, State),
  {ok, Resp, NewState};

init(Req, State) ->
  Resp = cowboy_req:reply(405, #{<<"allow">> => <<"POST">>}, Req),
  {ok, Resp, State}.


% convert any erlang term to string
erl_to_str(Term) -> lists:flatten(io_lib:format("~p", [Term])).
