use std::sync::Arc;
use axum::Router;
use axum::routing::post;
use crate::api::service::AppState;
use crate::api::router::endpoints_urls_consts::Endpoints;
use crate::api::router::handlers;

pub fn get_router(state: Arc<AppState>) -> Router {
    let router: Router = Router::new()
        .route(Endpoints::SH_EXECUTE, post(handlers::run_command))
        .with_state(state);
    
    router
}

