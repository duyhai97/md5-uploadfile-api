package demo.controller;

import demo.model.DBFile;
import demo.payload.UploadFileResponse;
import demo.service.DBFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@CrossOrigin
public class FileController {

//    private static final Logger logger = (Logger) LoggerFactory.getLogger(FileController.class);

    @Autowired
    private DBFileStorageService dbFileStorageService;

    @PostMapping(path = "/uploadFile", consumes = {MULTIPART_FORM_DATA_VALUE})
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        DBFile dbFile = dbFileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(dbFile.getId())
                .toUriString();

        return new UploadFileResponse(dbFile.getFileName(), fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @PostMapping("/uploadMultipleFiles")
    public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        return Arrays.asList(files)
                .stream()
                .map(file -> uploadFile(file))
                .collect(Collectors.toList());
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileId) {
        // Load file from database
        DBFile dbFile = dbFileStorageService.getFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(dbFile.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
                .body(new ByteArrayResource(dbFile.getData()));
    }

//    @GetMapping()
//    public ResponseEntity<ByteArrayResource> getAll(){
//        Iterable<DBFile> listFile =  dbFileStorageService.getAllFile();
//
//        for (Iterator<DBFile> i = listFile.iterator(); i.hasNext() ; ) {
//            // do something with i.next()
//            return ResponseEntity.ok().contentType(MediaType.parseMediaType(i.next().getFileType()))
//                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + i.next().getFileName())
//                    .body(new ByteArrayResource(i.next().getData()));
//        }
//
//
////        return new ResponseEntity<>( HttpStatus.OK);
//        return null;
//    }

//    @GetMapping()
//    public ResponseEntity<ByteArrayResource> downloadFile1() {
//        // Load file from database
//        Iterable<Iterable<DBFile>> dbFile = Collections.singleton(dbFileStorageService.getAllFile());
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(dbFile.getFileType()))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dbFile.getFileName() + "\"")
//                .body(new ByteArrayResource(dbFile.getData()));
//    }

    @GetMapping()
    public ResponseEntity<Iterable<DBFile>> findAll(){

        return new ResponseEntity<>(dbFileStorageService.getAllFile(), HttpStatus.OK);
    }

}