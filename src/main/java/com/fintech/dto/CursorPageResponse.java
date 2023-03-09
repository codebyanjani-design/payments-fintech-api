package com.fintech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CursorPageResponse<T> {
    private List<T> data;
    private Long nextCursor;
    private boolean hasNext;
}
