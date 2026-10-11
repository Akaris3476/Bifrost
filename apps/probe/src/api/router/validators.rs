use crate::api::router::handlers::RunCommandResponse;

pub fn validate_packages_args(args: &[String]) -> Result<(), RunCommandResponse> {
    
    if args.is_empty() {
        return Err(RunCommandResponse {
            stdout: String::new(),
            stderr: "Validation error. No packages specified".to_owned(),
            command_started: false,
            command_succeeded: false, 
            exit_code: None
        })
    }

    if args.len() > 5 {
        return Err(RunCommandResponse {
            stdout: String::new(),
            stderr: "Validation error. Too many packages specified".to_owned(),
            command_started: false,
            command_succeeded: false,
            exit_code: None
        })
    }

    let forbidden_chars = [';', '|', '&', '$', '`', '>', '<', '(', ')', '!', '\n', '\r'];
    
    for arg in args {
        if arg.starts_with("-"){
            return Err(RunCommandResponse {
                stdout: String::new(),
                stderr: format!("Validation error. Argument '{}' cant start with a dash (-)", arg),
                command_started: false,
                command_succeeded: false,
                exit_code: None
            })
        }
        
        if arg.trim().is_empty() {
            return Err(RunCommandResponse {
                stdout: String::new(),
                stderr: "Validation error. Package name cannot be empty or whitespace".to_owned(),
                command_started: false,
                command_succeeded: false,
                exit_code: None
            })
        }
        
        if arg.chars().any(|c| forbidden_chars.contains(&c)) {
            return Err(RunCommandResponse {
                stdout: String::new(),
                stderr: format!("Validation error. Argument '{}' cant contain forbidden characters", arg),
                command_started: false,
                command_succeeded: false,
                exit_code: None
            })
        }
    }

        
    Ok(())
}