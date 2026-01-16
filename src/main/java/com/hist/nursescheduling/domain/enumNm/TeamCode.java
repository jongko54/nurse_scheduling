package com.hist.nursescheduling.domain.enumNm;

public enum TeamCode {
    NSADPL("간호행정기획팀"),
    NSEDSU("간호교육지원팀"),
    NSIPT1("입원간호1팀"),
    NSIPT2("입원간호2팀"),
    NSIPT3("입원간호3팀"),
    NSIPT4("입원간호4팀"),
    NSSPC1("특수간호1팀"),
    NSSPC2("특수간호2팀"),
    NSOUTP("외래간호팀");

    private final String teamName;

    TeamCode(String teamName) {
        this.teamName = teamName;
    }

}
