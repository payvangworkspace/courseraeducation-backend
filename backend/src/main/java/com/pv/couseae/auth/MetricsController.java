package com.pv.couseae.auth;


import com.pv.couseae.security.HitCounterFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class MetricsController {

    private final HitCounterFilter hitCounterFilter;

    public MetricsController(HitCounterFilter hitCounterFilter) {
        this.hitCounterFilter = hitCounterFilter;
    }

    @GetMapping("/metrics/hits/total")
    public long totalHits() {
        return hitCounterFilter.getTotalHits();
    }

    @GetMapping("/metrics/hits/url")
    public Object hitsPerUrl() {
        return hitCounterFilter.getUrlHits().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().get()
                ));
    }

    @GetMapping("/metrics/hits/status")
    public Object hitsPerStatus() {
        return hitCounterFilter.getStatusHits().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().get()
                ));
    }
}
