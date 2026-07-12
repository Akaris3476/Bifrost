use serde::{Deserialize, Serialize};
use sysinfo::{System};

#[derive(Debug)]
#[derive(Serialize, Deserialize)]
pub struct MemoryMetrics{
    pub total_memory: u64,
    pub used_memory: u64,
    pub total_swap: u64,
    pub used_swap: u64,
}

pub fn collect_memory_metrics(sys: &mut System) -> MemoryMetrics {
    sys.refresh_memory();
    
    let total_memory = sys.total_memory();
    let used_memory = sys.used_memory();
    let total_swap = sys.total_swap();
    let used_swap = sys.used_swap();
    
    MemoryMetrics { total_memory, used_memory, total_swap, used_swap }
}
