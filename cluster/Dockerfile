FROM erlang:26

RUN apt update
RUN apt install -y \
	build-essential \
	libsqlite3-dev

# get rust
RUN curl https://sh.rustup.rs -sSf | sh -s -- -y
ENV PATH="/root/.cargo/bin:${PATH}"
RUN rustup default nightly

WORKDIR /app

# manually compile nif
COPY my_nif/ my_nif/
WORKDIR	/app/my_nif/native/my_nif
RUN --mount=type=cache,target=/root/.cargo/registry \
	cargo build --release
WORKDIR /app
RUN mkdir -p my_nif/priv/crates/my_nif/
RUN cp my_nif/native/my_nif/target/release/libmy_nif.so my_nif/priv/crates/my_nif/my_nif.so

# do the erlang stuff
COPY rebar.config .
COPY rebar.lock .
RUN --mount=type=cache,target=_build \
	rebar3 get-deps

COPY src/ src/
RUN --mount=type=cache,target=_build \
	rebar3 compile

COPY config/ config/
RUN --mount=type=cache,target=_build \
	rebar3 as prod release

RUN --mount=type=cache,target=_build \
	cp -r _build build

EXPOSE 8080

# this is the first part in the `-name`, the second is the hostname
# so the node name will be `ra@node$i.dsmt`
ENV NODE_NAME_NAME=ra
ENV LEADER=ra@node0.dsmt
ENV RELEASE_CURSOR_EVERY=-1
ENV RA_DATA_DIR=/ra-data/
ENV SQL_DATA_DIR=/sql-data/

CMD ["./build/prod/rel/erlang_project/bin/erlang_project", "foreground"]