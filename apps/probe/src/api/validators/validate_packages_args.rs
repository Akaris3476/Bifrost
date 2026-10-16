use axum::http::StatusCode;
use crate::api::handlers::run_sh_command::RunShCommandResponse;


pub fn validate_packages_args(args: &[String]) -> Result<(), RunShCommandResponse> {
    
    if args.is_empty() {
        return Err(RunShCommandResponse {
            stdout: String::new(),
            stderr: "Validation error. No packages specified".to_owned(),
            command_started: false,
            command_succeeded: false,
            exit_code: None,
            status_code: StatusCode::BAD_REQUEST.to_string()
        })
    }

    if args.len() > 5 {
        return Err(RunShCommandResponse {
            stdout: String::new(),
            stderr: "Validation error. Too many packages specified".to_owned(),
            command_started: false,
            command_succeeded: false,
            exit_code: None,
            status_code: StatusCode::BAD_REQUEST.to_string()
        })
    }

    let forbidden_chars = [';', '|', '&', '$', '`', '>', '<', '(', ')', '!', '\n', '\r'];
    
    for arg in args {
        if arg.starts_with("-"){
            return Err(RunShCommandResponse {
                stdout: String::new(),
                stderr: format!("Validation error. Argument '{}' cant start with a dash (-)", arg),
                command_started: false,
                command_succeeded: false,
                exit_code: None,
                status_code: StatusCode::BAD_REQUEST.to_string()
            })
        }
        
        if arg.trim().is_empty() {
            return Err(RunShCommandResponse {
                stdout: String::new(),
                stderr: "Validation error. Package name cannot be empty or whitespace".to_owned(),
                command_started: false,
                command_succeeded: false,
                exit_code: None,
                status_code: StatusCode::BAD_REQUEST.to_string()
            })
        }
        
        if arg.chars().any(|c| forbidden_chars.contains(&c)) {
            return Err(RunShCommandResponse {
                stdout: String::new(),
                stderr: format!("Validation error. Argument '{}' cant contain forbidden characters", arg),
                command_started: false,
                command_succeeded: false,
                exit_code: None,
                status_code: StatusCode::BAD_REQUEST.to_string()
            })
        }
    }

        
    Ok(())
}