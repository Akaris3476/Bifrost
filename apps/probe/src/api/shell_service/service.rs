use std::process::{Output};
use tokio::io::{Error};
use tokio::process::Command;


pub struct ShellService {
}
impl ShellService {
    pub fn new() -> Self {
        ShellService {  }
    }


    pub async fn install_package(&self, packages_name: &[String]) ->  Result<Output, Error> {
        let output_res = Command::new("apt")
            .args(["install", "-y"])
            .args(packages_name)
            .output()
            .await;
            
        output_res
    }
    

}
