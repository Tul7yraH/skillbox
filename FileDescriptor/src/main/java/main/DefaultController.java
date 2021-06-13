package main;

import main.model.Descriptor;
import main.model.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@Controller
public class DefaultController {

    @Autowired
    FileRepository fileRepository;

    @RequestMapping("/")
    public String index(Model model){
        Iterable<Descriptor> descriptorIterable = fileRepository.findAll();

        ArrayList<Descriptor> descriptorArrayList = new ArrayList<>();
        for(Descriptor descriptor: descriptorIterable){
            descriptorArrayList.add(descriptor);
        }
        model.addAttribute("files", descriptorArrayList);
        model.addAttribute("filesCount", descriptorArrayList.size());
        return "index";
    }
}
