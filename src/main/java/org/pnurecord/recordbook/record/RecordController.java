package org.pnurecord.recordbook.record;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/records")
@AllArgsConstructor
public class RecordController {
    private final RecordRepository recordRepository;
}
