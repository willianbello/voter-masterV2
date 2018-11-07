package br.edu.ulbra.election.voter.service;

import br.edu.ulbra.election.voter.exception.GenericOutputException;
import br.edu.ulbra.election.voter.input.v1.VoterInput;
import br.edu.ulbra.election.voter.model.Voter;
import br.edu.ulbra.election.voter.output.v1.GenericOutput;
import br.edu.ulbra.election.voter.output.v1.VoterOutput;
import br.edu.ulbra.election.voter.repository.VoterRepository;
import javassist.NotFoundException;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class VoterService {

    private final VoterRepository voterRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    private static final String MESSAGE_INVALID_ID = "Invalid id";
    private static final String MESSAGE_VOTER_NOT_FOUND = "Voter not found";
    private static final String MESSAGE_EMAIL_NOT_FOUND = "Email not found";
    private static final String MESSAGE_EMAIL_ALREADY_REGISTERED = "Email already registered";
    private static final String MESSAGE_EMAIL_IS_BLANK = "Email already registered";



    @Autowired
    public VoterService(VoterRepository voterRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.voterRepository = voterRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<VoterOutput> getAll() {
        Type voterOutputListType = new TypeToken<List<VoterOutput>>() {
        }.getType();
        return modelMapper.map(voterRepository.findAll(), voterOutputListType);
    }

    public VoterOutput create(VoterInput voterInput) {
        //validando email
        validateInput(voterInput, false);
        Voter voter = modelMapper.map(voterInput, Voter.class);
        //validando password
        voter.setPassword(validatePassword(voterInput.getPassword(), voterInput.getPasswordConfirm()));
        voter = voterRepository.save(voter);
        return modelMapper.map(voter, VoterOutput.class);
    }

    public VoterOutput getById(Long voterId) {
        if (voterId == null) {
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null) {
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }

        return modelMapper.map(voter, VoterOutput.class);
    }

    public Voter getByEmail(String email) {
        if (email == null) {
            throw new GenericOutputException(MESSAGE_EMAIL_NOT_FOUND);
        }

        Voter voter = voterRepository.findByEmail(email).orElse(null);
        //if (buscaEmail == null) {
        //    throw new GenericOutputException(MESSAGE_EMAIL_NOT_FOUND);
        //}
        return voter;
    }

    public VoterOutput update(Long voterId, VoterInput voterInput) {
        if (voterId == null) {
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }
        validateInput(voterInput, true);

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null) {
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }

        voter.setEmail(voterInput.getEmail());
        voter.setName(voterInput.getName());
        if (!StringUtils.isBlank(voterInput.getPassword())) {
            voter.setPassword(passwordEncoder.encode(voterInput.getPassword()));
        }
        voter = voterRepository.save(voter);
        return modelMapper.map(voter, VoterOutput.class);
    }

    public GenericOutput delete(Long voterId) {
        if (voterId == null) {
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null) {
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }

        voterRepository.delete(voter);

        return new GenericOutput("Voter deleted");
    }

    private String validatePassword(String password, String passwordConfirm) {
        if (StringUtils.isBlank(password) || (StringUtils.isBlank(passwordConfirm))) {
            throw new GenericOutputException("Password doesn't match");
        }
        if (!password.equals(passwordConfirm)) {
            throw new GenericOutputException("Password doesn't match");
        }

        return passwordEncoder.encode(password);
    }

    private void validateInput(VoterInput voterInput, boolean isUpdate){

        //Verificando se o email está vazio
        if (StringUtils.isBlank(voterInput.getEmail())){
            throw new GenericOutputException("Invalid email");
        }
        if (getByEmail(voterInput.getEmail()) != null && !isUpdate) {
            throw new GenericOutputException("Email alreary registered");
        }

        if (StringUtils.isBlank(voterInput.getName())){
            throw new GenericOutputException("Invalid name");
        }
          /*
        expressões para matches
        ^ verifica se a expressão começa com
        (?i: inicia um grupo com case-insensitive
        [a-z]+ verifica palavras com as letras de a-z até encontrar um espaço (note que após o + tem um espaço)
        [a-z ]+) verifica palavras com as letras de a-z e espaços, ou seja pode conter mais de uma palavra com espaço até encontrar o final do grupo (grupo para case-insensitive)
        $ verifica se a string termina exatamente conforme a expressão
         */
        if (!voterInput.getName().matches("^(?i:[a-z]+ [a-z ]+)$")){
            throw new GenericOutputException("Need a last name");
        }
        if (voterInput.getName().length() < 5){
            throw new GenericOutputException("Invalid name. Min. 5 letters");
        }
        if (!StringUtils.isBlank(voterInput.getPassword())){
            if (!voterInput.getPassword().equals(voterInput.getPasswordConfirm())){
                throw new GenericOutputException("Passwords doesn't match");
            }

        } else {
            if (!isUpdate) {
                throw new GenericOutputException("Password doesn't match");
            }
        }
    }

}
