#![feature(try_trait_v2)]
#![feature(fmt_internals)]

pub mod connection;
use std::collections::HashMap;
use std::fmt::Formatter;
use std::time::Duration;

use base64::Engine as _;
use connection::Result;
use connection::*;
use rustler::resource;
use rustler::resource::ResourceType;
use rustler::resource::ResourceTypeProvider;
use rustler::Decoder;
use rustler::Encoder;
use rustler::Env;
use rustler::NifResult;
use rustler::Term;

impl Encoder for SQLiteValue {
    fn encode<'a>(&self, env: Env<'a>) -> Term<'a> {
        match &self.0 {
            rusqlite::types::Value::Null => {
                let type_value: i32 = 0;
                (type_value, "null").encode(env)
            }
            rusqlite::types::Value::Integer(val) => {
                let type_value: i32 = 1;
                (type_value, val).encode(env)
            }
            rusqlite::types::Value::Real(val) => {
                let type_value: i32 = 2;
                (type_value, val).encode(env)
            }
            rusqlite::types::Value::Text(val) => {
                let type_value: i32 = 3;
                (type_value, val.as_str()).encode(env)
            }
            rusqlite::types::Value::Blob(val) => {
                let type_value: i32 = 4;
                let blob_string_value =
                    base64::engine::general_purpose::URL_SAFE_NO_PAD.encode(val);
                (type_value, blob_string_value).encode(env)
            }
        }
    }
}

impl<'a> Decoder<'a> for SQLiteValue {
    fn decode(term: Term<'a>) -> NifResult<Self> {
        let (type_value, value): (i32, Term<'a>) = term.decode()?;

        match type_value {
            0 => Ok(Self(rusqlite::types::Value::Null)),
            1 => {
                let int_value: i64 = value.decode()?;
                Ok(Self(rusqlite::types::Value::Integer(int_value)))
            }
            2 => {
                let float_value: f64 = value.decode()?;
                Ok(Self(rusqlite::types::Value::Real(float_value)))
            }
            3 => {
                let text_value: String = value.decode()?;
                Ok(Self(rusqlite::types::Value::Text(text_value)))
            }
            4 => {
                let blob_base64_value: String = value.decode()?;
                let blob_value = base64::engine::general_purpose::URL_SAFE_NO_PAD
                    .decode(blob_base64_value.as_bytes())
                    .map_err(|e| rustler::Error::Term(Box::new(e.to_string())))?;
                Ok(Self(rusqlite::types::Value::Blob(blob_value)))
            }
            _ => Err(rustler::Error::Term(Box::new(
                "invalid type value for SQLiteValue",
            ))),
        }
    }
}

impl Encoder for RusqliteError {
    fn encode<'a>(&self, env: Env<'a>) -> Term<'a> {
        let mut s = String::new();
        let mut fmt = Formatter::new(&mut s);
        match self {
            RusqliteError::RusqliteError(error) => {
                write!(fmt, "{}", "rusqlite: ").unwrap();
                std::fmt::Display::fmt(&error, &mut fmt).unwrap();
            }
            RusqliteError::CommunicationError(error) => {
                write!(fmt, "{}", "internal: ").unwrap();
                std::fmt::Display::fmt(&error, &mut fmt).unwrap();
            }

            RusqliteError::CustomError(error) => {
                write!(fmt, "{}", "internal: ").unwrap();
                std::fmt::Display::fmt(&error, &mut fmt).unwrap();
            }

            RusqliteError::IoError(error) => {
                write!(fmt, "{}", "io: ").unwrap();
                std::fmt::Display::fmt(&error, &mut fmt).unwrap();
            }
        };
        s.encode(env)
    }
}

impl std::error::Error for RusqliteError {}
