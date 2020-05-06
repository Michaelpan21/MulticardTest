package ru.muilticard.service;

import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.muilticard.entity.CrashDetails;
import ru.muilticard.repository.CrashDetailsRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AtmService {

    @Autowired
    private CrashDetailsRepo crashDetailsRepo;

    public Integer parseFile (Map<String, List<List<String>>> fileData) {
        ArrayList<CrashDetails> crashesList = new ArrayList<>();
        fileData.forEach((key, value) ->
            value.stream().skip(1).forEach(row -> {
                CrashDetails crashDetails = new CrashDetails();
                crashDetails.setId(Long.valueOf(row.get(0)));
                crashDetails.setAtmId(row.get(1));
                crashDetails.setAtmSerialNumber(row.get(5));
                crashDetails.setBankName(row.get(6));
                crashDetails.setReason(row.get(2));
                crashDetails.setBegin(DateUtil.getLocalDateTime(Double.valueOf(row.get(3))));
                crashDetails.setEnd(DateUtil.getLocalDateTime(Double.valueOf(row.get(4))));
                crashDetails.setChannel(row.get(7));
                crashesList.add(crashDetails);
            })
        );
        return crashDetailsRepo.saveAll(crashesList).size();
    }
}
