package com.espirit.moddev.cli.results;

import com.espirit.moddev.cli.commands.service.ProcessServiceInfo;

import java.util.List;

public class ServiceProcessResult extends SimpleResult<List<ProcessServiceInfo>> {
    public ServiceProcessResult(List<ProcessServiceInfo> processServiceInfos) {
        super(processServiceInfos);
    }

    public ServiceProcessResult(Exception e) {
        super(e);
    }
}
