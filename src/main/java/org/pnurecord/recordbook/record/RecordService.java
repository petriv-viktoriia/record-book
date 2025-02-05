package org.pnurecord.recordbook.record;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RecordService {
    private final RecordRepository recordRepository;

}
