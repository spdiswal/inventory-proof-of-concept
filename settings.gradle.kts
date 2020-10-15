rootProject.name = "inventory"

include("gateway:api")
include("gateway:application")
include("gateway:composition-root")
include("infrastructure:invariants")
include("infrastructure:messaging")
include("infrastructure:server-sent-events")
include("item:adapters")
include("item:api")
include("item:model")
include("item:service")
include("web-app")
