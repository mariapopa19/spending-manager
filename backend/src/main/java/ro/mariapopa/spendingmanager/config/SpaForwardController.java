package ro.mariapopa.spendingmanager.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {
    @GetMapping("/{path:[^\\.]*}")
    String forward() {
        return "forward:/index.html";
    }
}
