package com.hl.sf.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author hl2333
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class ServiceResult<T> {
    private boolean success;
    private String message;
    private T result;

}
