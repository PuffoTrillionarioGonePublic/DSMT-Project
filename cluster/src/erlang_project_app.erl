%%%-------------------------------------------------------------------
%% @doc erlang_project public API
%% @end
%%%-------------------------------------------------------------------

-module(erlang_project_app).

-behaviour(application).

-export([start/2, stop/1, wait_for_connection/2, try_add_member/2]).

-define(APP_NAME, erlang_project).

start(_Type, _Args) ->
  logger:set_primary_config(level, all),
  % ra config
  ClusterName = dsmt,
  Name = dsmt,
  MachineConfig = #{},
  Machine = {module, erlang_project_ra, MachineConfig},
  % start ra on all nodes regardless of whether they are leader or follower
  ok = ra:start(),
  Self = node(),
  logger:debug("Self: ~p, Leader ~p~n", [Self, application:get_env(?APP_NAME, leader)]),
  case ra:restart_server(default, {Name, Self}) of
    ok ->
      logger:info("Successfully restarted server"),
      ok;

    {error, Reason} ->
      logger:info("Unable to restart rever: ~p, starting from zero", [Reason]),
      % different start logic depending on whether we are leader or follower, die in case of fail
      ok =
        case application:get_env(?APP_NAME, leader) of
          % LEADER
          {ok, Self} ->
            logger:info("Starting as leader"),
            % start a single node cluster
            {ok, _Started, _Failed} =
              ra:start_cluster(default, ClusterName, Machine, [{Name, Self}]),
            ok;

          % FOLLOWER
          {ok, Leader} ->
            logger:info("Starting as follower"),
            % ensure we can connect to leader
            true = erlang_project_app:wait_for_connection(Leader, 10),
            % add self to cluster, try 10 times because if another node is joining at the same time, it might fail
            case
            erlang_project_app:try_add_member(
              fun () -> ra:add_member({Name, Leader}, {Name, Self}) end,
              10
            ) of
              {error, Reason} ->
                logger:error("Failed to add member: ~p", [Reason]),
                {error, Reason};

              {ok, RaIndex, _Leader} ->
                logger:info("Successfully added member to cluster, RaIndex: ~p", [RaIndex]),
                ra:start_server(default, ClusterName, {Name, Self}, Machine, [{Name, Leader}])
            end
        end;

    X -> logger:error("Failed to restart server: ~p", [X])
  end,
  CowboyHanderArgs = [{leader, {Name, Self}}],
  Dispatch = cowboy_router:compile([{'_', [{"/", erlang_project_cow, CowboyHanderArgs}]}]),
  {ok, Port} = application:get_env(?APP_NAME, port),
  {ok, _} =
    cowboy:start_clear(
      erlang_project_http_listener,
      [{port, Port}],
      #{env => #{dispatch => Dispatch}}
    ),
  erlang_project_sup:start_link().


stop(_State) -> ok.

% try MaxTries times to net_kernel:connect_node(Node), sleep 1 second between tries
wait_for_connection(Node, MaxTries) ->
  case net_kernel:connect_node(Node) of
    true -> true;

    false ->
      case MaxTries of
        0 -> false;

        _ ->
          timer:sleep(1000),
          wait_for_connection(Node, MaxTries - 1)
      end
  end.


% try MaxTries times to execute Fun(), sleep 1 second between tries
try_add_member(Fun, MaxTries) ->
  case Fun() of
    {ok, _, _} = Res -> Res;

    {error, _} = Res ->
      logger:error("try_add_member failed: ~p~n", [Res]),
      case MaxTries of
        0 -> Res;

        _ ->
          timer:sleep(1000),
          try_add_member(Fun, MaxTries - 1)
      end
  end.
