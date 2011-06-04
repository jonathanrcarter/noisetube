class ChangeLoudnessType < ActiveRecord::Migration
  def self.up
      change_column :measures, :loudness, :integer
  end

  def self.down
    change_column :measures, :loudness, :float
  end
end
