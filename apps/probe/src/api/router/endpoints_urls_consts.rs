pub struct Endpoints;

impl Endpoints 
{
    // OUTCOMING
    pub const SEND_METRICS: &str = "/api/v1/probe/metrics";
    pub const SEND_LOGS: &str = "/api/v1/probe/logs";
    // INCOMING
    pub const SH_EXECUTE: &str = "/probe/shell_execute";
    // pub const HEARTBEAT: &str = "/api/metrics";
    
}