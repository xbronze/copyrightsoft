// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract SoftwareEvidenceAnchor {

    struct EvidenceRecord {
        bytes32 metadataHash;
        address owner;
        uint256 timestamp;
        uint8 status;
        bool isRegistered;
    }

    mapping(bytes32 => EvidenceRecord) public records;

    event EvidenceRegistered(
        bytes32 indexed evidenceRootHash,
        bytes32 metadataHash,
        address indexed owner,
        uint256 timestamp,
        uint8 status
    );

    event EvidenceStatusUpdated(
        bytes32 indexed evidenceRootHash,
        uint8 oldStatus,
        uint8 newStatus,
        uint256 timestamp
    );

    function registerEvidence(
        bytes32 evidenceRootHash,
        bytes32 metadataHash,
        uint8 status
    ) external {
        require(!records[evidenceRootHash].isRegistered, "Evidence already exists.");
        records[evidenceRootHash] = EvidenceRecord({
            metadataHash: metadataHash,
            owner: msg.sender,
            timestamp: block.timestamp,
            status: status,
            isRegistered: true
        });
        emit EvidenceRegistered(evidenceRootHash, metadataHash, msg.sender, block.timestamp, status);
    }

    function queryEvidence(bytes32 evidenceRootHash) external view returns (
        bytes32 metadataHash,
        address owner,
        uint256 timestamp,
        uint8 status,
        bool isRegistered
    ) {
        EvidenceRecord memory record = records[evidenceRootHash];
        return (
            record.metadataHash,
            record.owner,
            record.timestamp,
            record.status,
            record.isRegistered
        );
    }

    function updateStatus(bytes32 evidenceRootHash, uint8 newStatus) external {
        EvidenceRecord storage record = records[evidenceRootHash];
        require(record.isRegistered, "Evidence not found.");
        require(record.owner == msg.sender, "Only owner can update status.");
        uint8 oldStatus = record.status;
        record.status = newStatus;
        emit EvidenceStatusUpdated(evidenceRootHash, oldStatus, newStatus, block.timestamp);
    }
}
