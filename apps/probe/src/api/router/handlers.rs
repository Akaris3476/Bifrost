use std::io::Error;
use std::os::unix::process::ExitStatusExt;
use std::process::{ExitStatus, Output};
use std::sync::Arc;
use axum::extract::State;
use axum::http::StatusCode;
use axum::Json;
use axum::response::{IntoResponse, Response};
use serde::{Deserialize, Serialize};
use crate::api::router::validators;
use crate::api::service::AppState;
use crate::api::shell_service::shell_commands::ShellCommands;
use crate::sysinfo_service::SysMetrics;

pub async fn test2(State(state): State<Arc<AppState>>, Json(metrics): Json<SysMetrics>)  {
    println!("RECEIVING METRICS");
    println!("{:?}", metrics);
}

pub async fn test() -> Response {
    println!("Hello test");

    Json("meow").into_response()
}

#[derive(Deserialize)]
pub struct RunCommandRequest{
    action_type: ShellCommands,
    args: Vec<String>
}
#[derive(Serialize)]
pub struct RunCommandResponse {
    pub stdout: String,
    pub stderr: String,
    pub command_succeeded: bool,
    pub command_started: bool,
    pub exit_code: Option<i32>,
}

pub async fn run_command(State(state): State<Arc<AppState>>, Json(req): Json<RunCommandRequest> ) -> Response {
    let shell_service = &state.shell_service;
    let action_type = &req.action_type;
    let args = &req.args;
    
    let res: Result<Output, Error> = match action_type {
        // ShellCommands::Restart => {
        //     println!("Restarting...");
        //     Ok(Output {
        //         status: ExitStatus::from_raw(0), 
        //         stdout: Vec::new(),
        //         stderr: Vec::new(),
        //     })
        // },
        ShellCommands::PkgInstall => {
            if let Err(err_res) = validators::validate_packages_args(args) {
                return (StatusCode::BAD_REQUEST, Json(err_res)).into_response()
            }
            
            println!("Installing {:?}...", args);
            shell_service.install_package(args).await
        }
        ShellCommands::PkgRemove => {
            if let Err(err_res) = validators::validate_packages_args(args) {
                return (StatusCode::BAD_REQUEST, Json(err_res)).into_response()
            }
            
            println!("Removing {:?}...", args);
            shell_service.remove_package(args).await
        }
    };
    
    match res {
        Ok(output) => {
            let status_code = if output.status.success() {
                println!("Command {:?} completed successfully", action_type);
                StatusCode::OK
            } else {
                println!("Command {:?} failed", action_type); 
                StatusCode::INTERNAL_SERVER_ERROR
            };
            
            let stdout = String::from_utf8_lossy(&output.stdout).to_string();
            let stderr = String::from_utf8_lossy(&output.stderr).to_string();

            println!("stdout: {}", stdout);
            println!("stderr: {}", stderr);
            
            let res = RunCommandResponse { stdout, stderr,
                command_succeeded: output.status.success(),
                command_started: true,
                exit_code: output.status.code() 
            };

            (status_code, Json(res)).into_response()
            
        }
        Err(io_err) => {
            println!("Command {:?} failed to start", action_type);
            
            let res = RunCommandResponse { 
                stdout: String::new(),
                stderr: io_err.to_string(),
                command_started: false,
                command_succeeded: false,
                exit_code: io_err.raw_os_error(),
            };

            (StatusCode::INTERNAL_SERVER_ERROR, Json(res)).into_response()
        }
    }
    
}