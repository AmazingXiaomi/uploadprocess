package com.process.springmvc.controller;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.sun.javaws.progress.Progress;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
@SessionAttributes("status")
public class ProgressController {

    //@RequestMapping(...)
    public String uploadFile(@RequestParam(value = "file") MultipartFile... files) throws IOException {
        for (MultipartFile f : files) {
            if (f.getSize() > 0) {
                File targetFile = new File("目标文件路径及文件名");
                f.transferTo(targetFile);//写入目标文件
            }
        }
        return "...";
    }

    @RequestMapping(value = "/upfile/progress", method = RequestMethod.POST )
    @ResponseBody
    public String initCreateInfo(Map<String, Object> model) {
        Progress status = (Progress) model.get("status");
        if(status==null){
            return "{}";
        }
        return status.toString();
    }
}