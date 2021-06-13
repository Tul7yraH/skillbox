package main.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;


@Entity
@Data
public class Descriptor {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "file_name")
    private String fileName;

    private String format;

    private Long size;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    public Descriptor(MultipartFile file) {
        this.fileName = file.getOriginalFilename();
        this.format = fileName.substring(fileName.indexOf("."));
        this.size = file.getSize();
        this.createdTime = LocalDateTime.now();
    }

    public Descriptor() {
    }


}