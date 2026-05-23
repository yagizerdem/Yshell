rootProject.name = "YSH"
include("src:main:test")
findProject(":src:main:test")?.name = "test"
