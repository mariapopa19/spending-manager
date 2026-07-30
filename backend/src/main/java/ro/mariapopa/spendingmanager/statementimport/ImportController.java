package ro.mariapopa.spendingmanager.statementimport;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ro.mariapopa.spendingmanager.transaction.Source;

@RestController
@RequestMapping("/api/imports")
@Tag(name = "Import")
public class ImportController {
  private final ImportService importService;

  public ImportController(ImportService importService) {
    this.importService = importService;
  }

  @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ImportPreviewResponse preview(
      @RequestParam Source source, @RequestParam MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      throw new StatementParseException("The upload file is empty");
    }
    try (InputStream in = file.getInputStream()) {
      return importService.preview(source, in);
    }
  }

  @PostMapping("/confirm")
  public ImportResultResponse confirm(@Valid @RequestBody ImportConfirmRequest request) {
    return importService.confirm(request.source(), request.personId(), request.rows());
  }
}
