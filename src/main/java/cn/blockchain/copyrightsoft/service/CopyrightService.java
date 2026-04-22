package cn.blockchain.copyrightsoft.service;

import cn.blockchain.copyrightsoft.dto.ApplyCopyrightRequest;
import cn.blockchain.copyrightsoft.dto.QueryResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CopyrightService {

    String applyCopyright(MultipartFile file, ApplyCopyrightRequest request);

    QueryResult queryByHash(String fileHash);

    QueryResult queryById(Long id);

    Page<QueryResult> getMyRecords(Integer page, Integer size, String keyword);
}
