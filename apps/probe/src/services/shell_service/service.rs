use std::process::{Output};
use tokio::sync::Mutex;
use tokio::io::{Error};
use tokio::process::Command;


pub struct ShellService {
    apt_lock: Mutex<()>
}
impl ShellService {
    pub fn new() -> Self {
        ShellService {
            apt_lock: Mutex::new(()),
        }
    }


    pub async fn install_package(&self, packages_name: &[String]) ->  Result<Output, Error> {
        let _guard = self.apt_lock.lock().await;
        
        let output_res = Command::new("apt-get")
            .env("DEBIAN_FRONTEND", "noninteractive")
            .args(["install", "-y"])
            .args(packages_name)
            .output()
            .await;
            
        output_res
    }
    
    pub async fn remove_package(&self, packages_name: &[String]) ->  Result<Output, Error> {
        let _guard = self.apt_lock.lock().await;

        let output_res = Command::new("apt-get")
            .env("DEBIAN_FRONTEND", "noninteractive")
            .args(["remove", "-y"])
            .args(packages_name)
            .output()
            .await;
        
        output_res
    }
    
    // pub async fn update_all_packages(&self) ->  Result<Output, Error> {
    //     
    // }
    

}
