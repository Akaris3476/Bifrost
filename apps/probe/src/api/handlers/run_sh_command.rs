use std::io::Error;
use std::process::{Output};
use std::sync::Arc;
use axum::http::StatusCode;
use crate::api::validators::validate_packages_args;
use crate::api::api_service::AppState;
pub(crate) use crate::api::contracts::shell::{RunShCommandRequest, RunShCommandResponse};
use crate::services::shell_service::shell_commands::ShellCommands;





pub async fn run_command(state: Arc<AppState>, req: RunShCommandRequest ) -> RunShCommandResponse {
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
            if let Err(err_res) = validate_packages_args::validate_packages_args(args) {
                return err_res
            }
            
            println!("Installing {:?}...", args);
            shell_service.install_package(args).await
        }
        ShellCommands::PkgRemove => {
            if let Err(err_res) = validate_packages_args::validate_packages_args(args) {
                return err_res
            }
            
            println!("Removing {:?}...", args);
            shell_service.remove_package(args).await
        }
    };
    
    match res {
        Ok(output) => {
            let status_code = if output.status.success() {
                println!("Command {:?} completed successfully", action_type);
                StatusCode::OK.to_string()
            } else {
                println!("Command {:?} failed", action_type); 
                StatusCode::INTERNAL_SERVER_ERROR.to_string()
            };
            
            let stdout = String::from_utf8_lossy(&output.stdout).to_string();
            let stderr = String::from_utf8_lossy(&output.stderr).to_string();

            println!("stdout: {}", stdout);
            println!("stderr: {}", stderr);
            
            let res = RunShCommandResponse { stdout, stderr,
                command_succeeded: output.status.success(),
                command_started: true,
                exit_code: output.status.code(),
                status_code
            };

            res
            
        }
        Err(io_err) => {
            println!("Command {:?} failed to start", action_type);
            
            let res = RunShCommandResponse { 
                stdout: String::new(),
                stderr: io_err.to_string(),
                command_started: false,
                command_succeeded: false,
                exit_code: io_err.raw_os_error(),
                status_code: StatusCode::INTERNAL_SERVER_ERROR.to_string()
            };

            res
        }
    }
    
}