package br.edu.ulbra.election.voter.repository;

import br.edu.ulbra.election.voter.model.Voter;
import br.edu.ulbra.election.voter.output.v1.VoterOutput;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VoterRepository extends CrudRepository<Voter, Long> {
    Optional<Voter> findByEmail(String email);
}
