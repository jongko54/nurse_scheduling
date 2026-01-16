package com.hist.nursescheduling.domain.enumNm;

public enum Department {
    // --- 진료과 (Medical Departments: MD로 시작) ---
    MDINTM("내과"),
    MDINFC("감염내과"),
    MDENDO("내분비내과"),
    MDRHEU("류마티스내과"),
    MDGAST("소화기내과"),
    MDNEPH("신장내과"),
    MDCARD("심장내과"),
    MDALRG("알레르기내과"),
    MDPULM("호흡기내과"),
    MDSURG("외과"),
    MDTRMA("외상외과"),
    MDTHOR("흉부외과"),
    MDNEUR("신경외과"),
    MDORTH("정형외과"),
    MDPLAS("성형외과"),
    MDURGY("비뇨의학과"),
    MDPEDI("소아청소년과"),
    MDOBGY("산부인과"),
    MDDERM("피부과"),
    MDOPHT("안과"),
    MDENTS("이비인후과"),
    MDPSYC("정신건강의학과"),
    MDNRGY("신경과"),
    MDANES("마취통증의학과"),
    MDRADO("영상의학과"),
    MDRONC("방사선종양학과"),
    MDNUCL("핵의학과"),
    MDREHB("재활의학과"),
    MDLABM("진단검사의학과"),
    MDPATH("병리과"),
    MDFAML("가정의학과"),
    MDEMER("응급의학과"),
    MDOCCU("직업환경의학과"),
    MDDENT("치과"),
    MDORAL("구강악안면외과"),
    MDPROS("치과보철과"),
    MDCONS("치과보존과"),
    MDOTDN("치과교정과"),
    MDCPHM("임상약리학과"),
    MDPREV("예방관리과"),
    MDHOSP("입원의학과"),

    // --- 센터 및 특수 부서 (Centers/Units: CT로 시작) ---
    CTDRUG("의약품안전센터"),
    CTRHAR("류마티스퇴행성관절염"),
    CTPUBL("공공전문진료센터"),
    CTMICB("마이크로바이옴센터"),
    CTVASC("혈관외과센터"),
    CTBEHV("행동발달증진센터"),
    CTDEVD("발달장애인거점병원"),
    CTHEAL("건강증진센터"),
    CTDIOB("당뇨&비만센터"),
    CTRHEM("류마티즘센터"),
    CTDIGE("소화기센터"),
    CTCBMT("암골수이식센터"),
    CTBRTH("유방갑상선외과센터"),
    CTREMC("권역응급의료센터"),
    CTLUNG("폐암센터"),
    CTICAM("인천국제공항의료센터"),
    CTAREC("항공응급의료콜자문센터"),
    CTNICU("신생아집중치료지역센터"),
    CTHOSP("호스피스완화의료센터"),
    CTSPIN("척추센터"),
    CTMDSC("의료기기부작용감시센터"),
    CTROBO("로봇수술센터"),
    CTORGT("장기이식센터"),
    CTORRM("수술실"),
    CTBONE("골조직은행"),
    CTINFC("감염관리실"),
    CTCANS("암통합지원센터"),
    CTPREC("정밀의료분석지원센터"),
    CTIICU("통합집중치료센터"),
    CTMICU("내과계집중치료실"),
    CTSICU("외과계집중치료실");

    private final String name;

    Department(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // 코드값으로 enum을 찾는 helper 메소드
    public static Department fromCode(String code) {
        for (Department deptcode : Department.values()) {
            if (deptcode.name().equals(code)) {
                return deptcode;
            }
        }
        return null;
    }
}