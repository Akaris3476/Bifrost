use clap::Parser;

#[derive(Parser, Debug)]
#[command(
    name = "Bifrost probe",
    version,
    about = "Lightweight daemon for server monitoring and remote execution ")]
pub struct ProbeConfig {
    /// Api key for backend
    #[arg(long, env = "API_KEY")]
    pub(crate) api_key: String,
    
    
    /// Bifrost server url to connect
    #[clap(long, env = "BIFROST_SERVER_URL")]
    pub(crate) remote_url: String,


}