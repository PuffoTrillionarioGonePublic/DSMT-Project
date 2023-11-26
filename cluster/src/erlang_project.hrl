-record(
  app_state,
  {
    index :: ra:index(),
    term :: ra:term(),
    ctx :: term(),
    conns :: #{binary() => term()},
    stmts :: #{binary() => term()}
  }
).

-type app_state() :: #app_state{}.

% args, return value and function type for `api` functions
-type api_args() :: #{ArgName :: binary() => ArgVal :: term()}.
-type api_ret() :: jsx:json_term().
-type api_fun() :: fun((app_state(), api_args()) -> {NewState :: app_state(), api_ret()}).
