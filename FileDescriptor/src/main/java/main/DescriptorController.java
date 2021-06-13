package main;

import main.model.Descriptor;
import main.model.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class DescriptorController {

    private static final String PATH = ".FILES/";
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private ServletContext servletContext;

    @GetMapping(value="/files")
    public List<String> getFiles() {
        Iterable<Descriptor> descriptorIterable =  fileRepository.findAll();
        ArrayList<String> descriptorArrayList = new ArrayList<>();
        for(Descriptor descriptor: descriptorIterable){
            descriptorArrayList.add(descriptor.getFileName());
        }
        return descriptorArrayList;
    }

    @PostMapping(value="/files")
    public String addFile(@RequestParam("file") MultipartFile file){
        if (!file.isEmpty()) {
            try {
                String name = file.getOriginalFilename();

                Descriptor descriptor = new Descriptor(file);
                fileRepository.save(descriptor);
                descriptor.setFileName(descriptor.getId() + "-" + name);
                fileRepository.save(descriptor);

                file.transferTo(Paths.get(PATH + descriptor.getFileName()));

                return "Вы удачно загрузили " + name + "!";
            } catch (Exception e) {
                return "Вам не удалось загрузить файл => " + e.getMessage();
            }
        } else {
            return "Вам не удалось загрузить файл потому что файл пустой.";
        }
    }

    @GetMapping(value="/files/{id}/descriptor")
    public ResponseEntity getFile(@PathVariable int id){
        Optional<Descriptor> optionalAffair = fileRepository.findById(id);
        boolean isPresent = optionalAffair.isPresent();
        return isPresent
                ? new ResponseEntity(optionalAffair.get(), HttpStatus.OK)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    private void deleteFileInCatalog
            (String name){
        Path path = Paths.get(PATH + name);
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DeleteMapping("/files/{id}")
    public ResponseEntity deleteFile(@PathVariable int id) {
        Optional<Descriptor> optionalAffair = fileRepository.findById(id);
        if(optionalAffair.isPresent()){
            deleteFileInCatalog(optionalAffair.get().getFileName());
            fileRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @DeleteMapping("/files")
    public ResponseEntity deleteAllFiles() {
        for (String name : getFiles()) {
            deleteFileInCatalog(name);
        }
        fileRepository.deleteAll();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @PutMapping(value = "/files/{id}")
    public ResponseEntity updateFile(@PathVariable int id,@RequestParam("file") MultipartFile newFile) throws IOException {

        Optional<Descriptor> optionalDescriptor = fileRepository.findById(id);

        if(optionalDescriptor.isPresent() && !newFile.isEmpty()){
            Descriptor tempFile = optionalDescriptor.get();

            File oldFile = new File(PATH + tempFile.getFileName());
            oldFile.delete();

            tempFile.setFileName(tempFile.getId() + "-" + newFile.getOriginalFilename());
            tempFile.setSize(newFile.getSize());
            tempFile.setFormat(tempFile.getFileName().substring( tempFile.getFileName().indexOf(".")));
            tempFile.setCreatedTime(LocalDateTime.now());

            fileRepository.save(tempFile);

            BufferedOutputStream stream =
                    new BufferedOutputStream
                            (new FileOutputStream(new File(PATH + tempFile.getFileName())));
            stream.write(newFile.getBytes());
            stream.close();
            return new ResponseEntity(optionalDescriptor.get(), HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(null);
    }

    @GetMapping(value = "/files/{id}")
    public ResponseEntity<InputStreamResource>
    downloadFile(@PathVariable int id ) throws IOException {
        Optional<Descriptor> optionalDescriptor = fileRepository.findById(id);

        if(optionalDescriptor.isPresent()){
            String fileName = optionalDescriptor.get().getFileName();

            MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(servletContext, fileName);

            File file = new File(PATH + fileName);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            return ResponseEntity.status(HttpStatus.OK)
                    // Content-Disposition
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                    // Content-Type
                    .contentType(mediaType)
                    // Contet-Length
                    .contentLength(file.length()) //
                    .body(resource);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
}
