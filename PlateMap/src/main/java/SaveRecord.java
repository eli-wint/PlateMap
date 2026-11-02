package main.java;

import jakarta.persistence.*;

@Entity
@Table(name = "SAVE_RECORD",
       uniqueConstraints = @UniqueConstraint(columnNames = {"save_name", "state_code", "variant_name"}))
public class SaveRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "save_name", nullable = false)
    private String saveName;

    @Column(name = "state_code", nullable = false)
    private String stateCode;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    public SaveRecord() {}

    public SaveRecord(String saveName, String stateCode, String variantName) {
        this.saveName = saveName;
        this.stateCode = stateCode;
        this.variantName = variantName;
    }

    public String getSaveName() { return saveName; }
    public String getStateCode() { return stateCode; }
    public String getVariantName() { return variantName; }
}