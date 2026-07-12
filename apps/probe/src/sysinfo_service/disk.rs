use std::path::{PathBuf};
use serde::{Deserialize, Serialize};
use sysinfo::{Disks};

#[derive(Debug)]
#[derive(Serialize, Deserialize)]
pub struct DiskMetrics {
    pub name: String,
    pub total_space: u64,
    pub available_space: u64,
    pub mount_point: PathBuf,
    pub file_system: String,
    
    // TODO: I/O disk usage??
}

pub fn collect_disks_metrics(disks: &mut Disks) -> Box<[DiskMetrics]> {
    // TODO: optimize with RefreshKind
    disks.refresh(true);
    
    let mut disk_list: Vec<DiskMetrics> = Vec::new();
    for disk in disks {
        
        
        disk_list.push( DiskMetrics {
            name: disk.name().to_string_lossy().into_owned(),
            total_space: disk.total_space(),
            available_space: disk.available_space(),
            mount_point: disk.mount_point().to_path_buf(),
            file_system: disk.file_system().to_string_lossy().into_owned(),
        })
    }
    
    disk_list.into_boxed_slice()
}


