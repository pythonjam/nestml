# -*- coding: utf-8 -*-
#
# co_co_each_block_unique_and_defined.py
#
# This file is part of NEST.
#
# Copyright (C) 2004 The NEST Initiative
#
# NEST is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
#
# NEST is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with NEST.  If not, see <http://www.gnu.org/licenses/>.
from pynestml.meta_model.ast_neuron import ASTNeuron
from pynestml.cocos.co_co import CoCo
from pynestml.utils.logger import Logger, LoggingLevel
from pynestml.utils.messages import Messages


class CoCoEachBlockUniqueAndDefined(CoCo):
    """
    This context  condition ensures that each block is defined at most once.
    Not allowed:
        state:
            ...
        end
        ...
        state:
            ...
        end
    """

    @classmethod
    def check_co_co(cls, neuron):
        """
        Checks whether each block is define at most once.
        :param neuron: a single neuron.
        :type neuron: ASTNeuron
        """
        assert (neuron is not None and isinstance(neuron, ASTNeuron)), \
            '(PyNestML.CoCo.BlocksUniques) No or wrong type of neuron provided (%s)!' % type(neuron)
        if isinstance(neuron.get_state_blocks(), list) and len(neuron.get_state_blocks()) > 1:
            code, message = Messages.get_block_not_defined_correctly('State', False)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that update block is defined at most once
        if isinstance(neuron.get_update_blocks(), list) and len(neuron.get_update_blocks()) > 1:
            code, message = Messages.get_block_not_defined_correctly('Update', False)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that parameters block is defined at most once
        if isinstance(neuron.get_parameter_blocks(), list) and len(neuron.get_parameter_blocks()) > 1:
            code, message = Messages.get_block_not_defined_correctly('Parameters', False)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that internals block is defined at most once
        if isinstance(neuron.get_internals_blocks(), list) and len(neuron.get_internals_blocks()) > 1:
            code, message = Messages.get_block_not_defined_correctly('Internals', False)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that equations block is defined at most once
        if isinstance(neuron.get_equations_blocks(), list) and len(neuron.get_equations_blocks()) > 1:
            code, message = Messages.get_block_not_defined_correctly('Equations', False)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        # check that input block is defined at most once
        if isinstance(neuron.get_input_blocks(), list) and len(neuron.get_input_blocks()) > 1:
            code, message = Messages.get_block_not_defined_correctly('Input', False)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        elif isinstance(neuron.get_input_blocks(), list) and len(neuron.get_input_blocks()) == 0:
            code, message = Messages.get_block_not_defined_correctly('Input', True)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.WARNING)
        elif neuron.get_input_blocks() is None:
            code, message = Messages.get_block_not_defined_correctly('Input', True)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.WARNING)
        # check that output block is defined at most once
        if isinstance(neuron.get_output_blocks(), list) and len(neuron.get_output_blocks()) > 1:
            code, message = Messages.get_block_not_defined_correctly('Output', False)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.ERROR)
        elif isinstance(neuron.get_output_blocks(), list) and len(neuron.get_output_blocks()) == 0:
            code, message = Messages.get_block_not_defined_correctly('Output', True)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.WARNING)
        elif neuron.get_output_blocks() is None:
            code, message = Messages.get_block_not_defined_correctly('Output', True)
            Logger.log_message(code=code, message=message, node=neuron, error_position=neuron.get_source_position(),
                               log_level=LoggingLevel.WARNING)
