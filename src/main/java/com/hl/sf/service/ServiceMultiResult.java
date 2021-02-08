package com.hl.sf.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author hl2333
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceMultiResult<T> {
    private Long total;
    private List<T> result;

    public int getResultSize(){
        if(this.result == null){
            return 0;
        }
        return this.result.size();
    }
}
