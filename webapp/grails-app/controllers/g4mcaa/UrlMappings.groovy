package g4mcaa

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(redirect:"/member/index")
        "500"(view:'/view500')
        "404"(view:'/view404')
    }
}
