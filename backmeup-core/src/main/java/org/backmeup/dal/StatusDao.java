package org.backmeup.dal;

import java.util.Date;
import java.util.List;

import org.backmeup.model.Status;

public interface StatusDao extends BaseDao<Status> {

    List<Status> findLastByJob(String username, Long jobId);

    List<Status> findByJobId(Long jobId);

    void deleteBefore(Long jobId, Date timeStamp);
}
