package org.pnurecord.recordbook.record;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/records")
public class RecordController {
    private final RecordService recordService;
}
